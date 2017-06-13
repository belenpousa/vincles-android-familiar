/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.mobile.model;

import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.TaskService;
import cat.bcn.vincles.lib.dao.TaskDAO;
import cat.bcn.vincles.lib.dao.TaskDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.activity.VinclesActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskModel {
    private static final String TAG = "TaskModel";
    protected MainModel mainModel = MainModel.getInstance();
    private boolean initialized;
    private static TaskModel instance;
    private TaskDAO taskDAO;
    public String view;
    public static final String TASK_DETAIL = "taskDetail";
    public Task currentTask;

    public static TaskModel getInstance() {
        if (instance == null) {
            instance = new TaskModel();
            instance.initialize();
        }
        return instance;
    }

    private TaskModel() {
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;
            view = "";
            taskDAO = new TaskDAOImpl();
            getTaskList();
            currentTask = new Task();
        }
    }

    public List<Task> getTaskList() {
        Log.i(TAG, "getTaskList()");
        if (mainModel.currentNetwork == null) return new ArrayList<Task>();
        return taskDAO.findByNetwork(mainModel.currentNetwork.getId());
    }

    public List<Task> getTodayTaskList() {
        if (mainModel.currentNetwork == null) return new ArrayList<Task>();

        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 1);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public List<Task> getTodayTaskListAllNetworks() {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 1);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), null);
    }

    public void getTodayTaskServerList(AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response, mainModel.currentNetwork, dateFrom, dateTo);
    }

    public List<Task> getTomorrowTaskList() {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.add(Calendar.DATE, 1);
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 2);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public void getTomorrowTaskServerList(AsyncResponse response) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.add(Calendar.DATE, 1);
        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.add(Calendar.DATE, 2);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response, mainModel.currentNetwork, dateFrom, dateTo);
    }

    public List<Task> getSelectedTaskList(Date date) {
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(date);
        calFrom = VinclesConstants.getCalendarWithoutTime(calFrom);
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(date);
        calTo = VinclesConstants.getCalendarWithoutTime(calTo);
        calTo.add(Calendar.DATE, 1);

        return taskDAO.findByRangeDate(calFrom.getTime(), calTo.getTime(), mainModel.currentNetwork);
    }

    public void getSelectedTaskServerList(AsyncResponse response, Date date) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = Calendar.getInstance();
        calFrom.setTime(date);
        calFrom = VinclesConstants.getCalendarWithoutTime(calFrom);
        Calendar calTo = Calendar.getInstance();
        calTo.setTime(date);
        calTo = VinclesConstants.getCalendarWithoutTime(calTo);
        calTo.add(Calendar.DATE, 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);
        getTaskServerList(response,  mainModel.currentNetwork, dateFrom, dateTo);
    }

    public List<Task> getMonthTask(int month, int year) {
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.set(Calendar.YEAR, year);
        calFrom.set(Calendar.DATE, 1);
        calFrom.set(Calendar.MONTH, month);
        Date dateFrom = calFrom.getTime();

        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.set(Calendar.YEAR, year);
        calTo.set(Calendar.DATE, 1);
        calTo.set(Calendar.MONTH, month + 1);
        Date dateTo = calTo.getTime();

        return taskDAO.findByRangeDate(dateFrom, dateTo, mainModel.currentNetwork);
    }

    public void getMonthTaskServerList(AsyncResponse response, int month, int year) {
        if (MainModel.avoidServerCalls) return;
        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calFrom.set(Calendar.YEAR, year);
        calFrom.set(Calendar.DATE, 1);
        calFrom.set(Calendar.MONTH, month);

        Calendar calTo = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        calTo.set(Calendar.YEAR, year);
        calTo.set(Calendar.DATE, 1);
        calTo.set(Calendar.MONTH, month + 1);

        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        String dateTo = String.valueOf(calTo.getTime().getTime()-1);

        getTaskServerList(response, mainModel.currentNetwork, dateFrom, dateTo);
    }

    public void getAllTaskServer(AsyncResponse response, Network network, long dateFrom) {
        getAllTaskServerRecursive(response, network, ""+dateFrom, "9999999999999");
    }

    private void getAllTaskServerRecursive(final AsyncResponse response, final Network network, final String dateFrom, String to) {
        AsyncResponse responseFake = new AsyncResponse() {
            @Override public void onSuccess(Object result) {
                List<Task> items = (List<Task>)result;
                if (items.size() == 10) // MAX ITEMS
                {
                    getAllTaskServerRecursive(response, network, dateFrom, ""+items.get(0).getDate().getTime());
                }
                else response.onSuccess(result);
            }
            @Override public void onFailure(Object error) {
                response.onFailure(error);
            }
        };
        getTaskServerList(responseFake, network, dateFrom, to);
    }

    public boolean getTaskServerList(final AsyncResponse response, final Network network, String dateFrom, String dateTo) {
        if (MainModel.avoidServerCalls || network.userVincles == null) return false;
        Log.i(TAG, "getTaskServerList()");
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<JsonArray> call = client.getEventList(network.userVincles.idCalendar, dateFrom, dateTo);
        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> result) {
                if (result.isSuccessful()) {
                    JsonArray jsonArray = result.body();
                    List<Task> items = new ArrayList<>();
                    for (JsonElement it : jsonArray) {
                        Task item = Task.fromJSON(it.getAsJsonObject());
                        item.network = network;

                        JsonElement userCreatorJSON = it.getAsJsonObject().get("userCreator");
                        if (userCreatorJSON != null && userCreatorJSON.isJsonNull() == false) {
                            User userCreator = User.fromJSON(userCreatorJSON.getAsJsonObject());
                            item.owner = userCreator;
                        }
                        items.add(item);
                    }

                    // Sort by date
                    Collections.sort(items, new Comparator<Task>() {
                        @Override
                        public int compare(Task o1, Task o2) {
                            return o1.getDate().compareTo(o2.getDate());
                        }
                    });

                    response.onSuccess(items);
                    saveOrUpdateTaskList(items);
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

    // Synchronize list!
    private void saveOrUpdateTaskList(List<Task> items) {
        // CAUTION: Create only new items!!!
        for (Task item : items) {
            saveTask(item);
        }
    }

    public void sendTask(final AsyncResponse response, final Task task) {
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<JsonObject> call = client.createTask(task.calendarId, task.toJSONNoOnwer());
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    Long id = result.body().get("id").getAsLong();
                    task.setId(id);
                    saveTask(task);
                    response.onSuccess(task);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                response.onFailure(t);
            }
        });
    }

    public void updateTask(final AsyncResponse response, final Task task) {
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.updateTask(task.calendarId, task.getId(), task.toJSONNoOnwer());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    saveTask(task);
                    response.onSuccess(task);
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

    public void saveTask(Task item) {
        // Save first 'userCreator' (must be persisted separated by the ORM)
        if (item.network == null)
            item.network = mainModel.currentNetwork;
        Task oldTask = taskDAO.get(item.getId());
        if (oldTask != null) item.androidCalendarId = oldTask.androidCalendarId;
        taskDAO.save(item);
    }

    public void deleteTask(Task item) {
        taskDAO.delete(item);
    }

    public void rememberTask(final Task task) {
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.rememberTask(task.calendarId, task.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "result: " + result.body());
                mainModel.showSimpleError(VinclesActivity.instance.findViewById(R.id.main_content),
                        VinclesActivity.instance.getString(R.string.task_remember_submitted),
                        Snackbar.LENGTH_LONG);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = mainModel.getErrorByCode(t);
                Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
                mainModel.showSimpleError(VinclesActivity.instance.findViewById(R.id.main_content),
                        errorMessage,
                        Snackbar.LENGTH_LONG);
            }
        });
    }

    public void deleteTaskServer(final Task task) {
        TaskService client = ServiceGenerator.createService(TaskService.class, mainModel.accessToken);
        Call<ResponseBody> call = client.deleteTask(task.calendarId, task.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Log.i(TAG, "result: " + result.body());
//                Toast toast = Toast.makeText(mainModel.context, mainModel.context.getResources().getString(R.string.task_deleted), Toast.LENGTH_SHORT);
//                toast.show();
                // Delete local 'Task'
                deleteTask(task);
                mainModel.showSimpleError(VinclesActivity.instance.findViewById(R.id.main_content),
                        VinclesActivity.instance.getString(R.string.task_delete_msg),
                        Snackbar.LENGTH_LONG);

                AndroidCalendarModel.getInstance()
                        .deleteAndroidCalendarEvent(task, VinclesActivity.instance);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = mainModel.getErrorByCode(t);
                mainModel.showSimpleError(VinclesActivity.instance.findViewById(R.id.main_content),
                        errorMessage,
                        Snackbar.LENGTH_LONG);
            }
        });
    }
}