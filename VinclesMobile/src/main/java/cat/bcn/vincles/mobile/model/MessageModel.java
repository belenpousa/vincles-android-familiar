/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.MessageService;
import cat.bcn.vincles.lib.business.ResourceService;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.dao.MessageDAO;
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.dao.ResourceDAO;
import cat.bcn.vincles.lib.dao.ResourceDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageModel {
    private static final String TAG = "MessageModel";
    protected MainModel mainModel = MainModel.getInstance();
    private boolean initialized;
    private static MessageModel instance;
    private MessageDAO messageDAO;
    private ResourceDAO resourceDAO;
    public String view;
    public static final String MESSAGE_DETAIL = "messageDetail";
    public Message currentMessage;

    public static MessageModel getInstance() {
        if (instance == null) {
            instance = new MessageModel();
            instance.initialize();
        }
        return instance;
    }

    private MessageModel() {
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;
            view = "";
            messageDAO = new MessageDAOImpl();
            resourceDAO = new ResourceDAOImpl();
            currentMessage = new Message();
        }
    }

    public List<Message> getMessageList() {
        Log.i(TAG, "getMessageList()");

        if (mainModel.currentUser == null || mainModel.currentNetwork == null)
            return new ArrayList<Message>();
        List<Message> items =  messageDAO.findByMeAndUserVincles(mainModel.currentUser, mainModel.currentNetwork.userVincles);
        // Populate userFrom for each message
        for (Message it : items) {
            it.userFrom = mainModel.getUser(it.idUserFrom);
        }

        return items;
    }

    public void getAllMessagesServer(AsyncResponse response, long dateFrom) {
        getAllMessagesServerRecursive(response, ""+dateFrom, ""+new Date().getTime());
    }

    private void getAllMessagesServerRecursive(final AsyncResponse response, final String dateFrom, String to) {
        AsyncResponse responseFake = new AsyncResponse() {
            @Override public void onSuccess(Object result) {
                List<Message> items = (List<Message>)result;
                if (items.size() == 10) // MAX ITEMS
                {
                    getAllMessagesServerRecursive(response, dateFrom, ""+items.get(9).sendTime.getTime());
                }
                else response.onSuccess(result);
            }
            @Override public void onFailure(Object error) {
                response.onFailure(error);
            }
        };
        getMessageServerList(responseFake, dateFrom, to);
    }

    public boolean getMessageServerList(final AsyncResponse response, String dateFrom, String dateTo) {
        if (MainModel.avoidServerCalls
                || mainModel.currentNetwork == null
                || mainModel.currentNetwork.userVincles == null) return false;
        Log.i(TAG, "getMessageServerList()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getMessageList(dateFrom, dateTo, null);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Message> items = new ArrayList<Message>();
                    for (JsonElement it : jsonArray) {
                        Message item = Message.fromJSON(it.getAsJsonObject());

                        // CAUTION: Check if Message has 'metadataTipus'
                        if (VinclesConstants.hasKnownType(item.metadataTipus)) {
                            item.userFrom = mainModel.getUser(item.idUserFrom);
                            items.add(item);
                        } else {
                            Log.e("getMessageServerList", "message " + item.getId() + "has no metadataTipus or is unknown!");
                        }

                    }
                    saveOrUpdateMessageList(items);
                    response.onSuccess(items);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                response.onFailure(t);
            }
        });
        return true;
    }

    public void saveMessage(Message item) {
        List<Message> temp = new ArrayList<>();
        temp.add(item);
        saveOrUpdateMessageList(temp);
    }

    public void sendMessage(final AsyncResponse response, final Message message) {
        Log.i(TAG, "sendMessage()");
        if (message.getResources().size() > 0) {
            // Synchronize Send of all resources
            List<Resource> items = new ArrayList<Resource>(message.resourceTempList);
            final List<Resource> idList = new ArrayList<Resource>();

            sendResources(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Replace collection without id's
                    message.resourceTempList = idList;
                    sendMessageToServer(response, message);
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendMessage() - error: " + error);
                    response.onFailure(error);
                }
            }, items, idList);
        } else {
            // No data, only text
            sendMessageToServer(response, message);
        }
    }

    private Call call;

    private void sendMessageToServer(final AsyncResponse response, Message message) {
        Log.i(TAG, "sendMessageToServer()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        //Call<ResponseBody> call = client.sendMessage(message.toJSON());
        call = client.sendMessage(message.toJSON());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String messageId = json.get("id").getAsString();

                    response.onSuccess(messageId);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendMessageToServer() - error: " + t.getMessage());

                // Check for cancel operations!
                if (call.isCanceled()) {
                    response.onFailure(VinclesError.ERROR_CANCEL);
                } else {
                    response.onFailure(t);
                }
            }
        });
    }

    private void sendResources(final AsyncResponse response, final List<Resource> inputList, final List<Resource> resultList) {
        if (inputList.size() > 0) {
            final Resource item = inputList.get(0);
            inputList.remove(0);
            sendResource(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Update resource id
                    item.setId(Long.parseLong((String) result));
                    resultList.add(item);
                    sendResources(response, inputList, resultList);
                }

                @Override
                public void onFailure(Object error) {
                    response.onFailure(error);
                }
            }, item.data);
        } else {
            response.onSuccess(resultList);
        }
    }

    private void sendResource(final AsyncResponse response, MultipartBody.Part data) {
        Log.i(TAG, "sendResource()");
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        call = client.sendResource(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String resourceId = json.get("id").getAsString();
                    response.onSuccess(resourceId);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "sendResource() - error: " + t.getMessage());

                // Check for cancel operations!
                if (call.isCanceled()) {
                    response.onFailure(VinclesError.ERROR_CANCEL);
                } else {
                    response.onFailure(t);
                }
            }
        });
    }

    public void saveResource(Resource item) {
        resourceDAO.save(item);
    }

    private void saveOrUpdateMessageList(List<Message> items) {
        // Only save/create only new items!!!
        for (Message item : items) {
            // Only update new Resources!!!
            for (Resource it : item.getResources()) {
                Resource re = resourceDAO.get(it.getId());
                if (re == null) {
                    it.message = item;
                    saveResource(it);
                }
            }
            messageDAO.save(item);
        }
    }

    public void getServerResourceData(final AsyncResponse response, final Long resourceId) throws IOException {
        ResourceService client = ServiceGenerator.createService(ResourceService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.getResource(resourceId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = new byte[0];
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    response.onSuccess(data);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void cancelSendMessage() {
        if (call != null) {
            call.cancel();
        }
    }

    public void deleteMessage(Message item) {
        // Delete associate resources
        for (Resource resource : item.getResources()) {
            switch (item.metadataTipus) {
                case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                    VinclesConstants.deleteImage(resource.filename);
                    break;
                case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                    VinclesConstants.deleteAudio(resource.filename);
                    break;
                case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                    VinclesConstants.deleteVideo(resource.filename);
                    break;
                case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                    VinclesConstants.deleteImage(resource.filename);
                    break;
            }
            resourceDAO.delete(resource);
        }
        // Delete message
        messageDAO.delete(item);
    }

    public void markMessageAsWatched(final Message item) {
        Log.i(TAG, "markMessageAsWatched()");
        MessageService client = ServiceGenerator.createService(MessageService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.markMessage(item.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    // Update message 'watched'
                    item.watched = true;
                    saveMessage(item);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    String errorMessage = mainModel.getErrorByCode(errorCode);
                    Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String errorMessage = mainModel.getErrorByCode(t);
                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}