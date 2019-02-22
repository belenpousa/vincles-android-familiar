package cat.bcn.vincles.mobile.UI.Chats;

import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;

import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_AUDIO;

public class ChatAudioRecorderFragment extends Fragment {

  MediaRecorder audioRecorder;
  CountDownTimer audioTimer;

  ChatPresenterContract presenter;
  ChatFragmentView view;
  String audioPath;

  boolean saveMedia = false;
  boolean clickSave = false;

  void setPresenterAudio(ChatPresenterContract presenter, ChatFragmentView view) {
    this.presenter = presenter;
    this.view = view;

    if (clickSave && presenter != null) presenter.onClickSendAudio();
    else if (saveMedia && presenter != null) presenter.onSaveMediaFile(audioPath, MEDIA_AUDIO);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onDetach() {
    super.onDetach();
    stopRecording(false);
    presenter = null;
    view = null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    stopRecording(false);

    if (audioRecorder != null) {
      audioRecorder.release();
      audioRecorder = null;
    }
    if (audioTimer != null) {
      audioTimer.cancel();
      audioTimer = null;
    }
  }

  void startRecording() {
    stopRecording(false);
    try {
      audioPath = ImageUtils.createAudioFile(MyApplication.getAppContext()).getPath();

      audioRecorder = new MediaRecorder();
      audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      audioRecorder.setOutputFile(audioPath);
      audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

      audioRecorder.prepare();

      try {
        audioRecorder.start();
      } catch (Exception e) {
        Log.d("ERROR", "Error:"+e);
      }

      audioTimer = new CountDownTimer(60*1000,16) {
        @Override
        public void onTick(long millisUntilFinished) {
          Log.d("qwer","audio timer tick");
          if (view != null) view.setAudioProgress(60*1000-(int)millisUntilFinished,
            DateUtils.getFormatedTimeFromMillis(60*1000-(int)millisUntilFinished));
        }

        @Override
        public void onFinish() {
          try {
            if (view != null) view.setAudioProgress(60*1000,
              DateUtils.getFormatedTimeFromMillis(60*1000));
            if (presenter != null) {
              presenter.onClickSendAudio();
            } else {
              clickSave = true;
            }
          } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
          }
        }
      }.start();
    } catch (IOException e) {
      Log.e("audio", "could not write to file");
    }
  }

  void stopRecording(boolean save) {
    if (audioTimer != null) {
      audioTimer.cancel();
      audioTimer = null;
    }
    if (audioRecorder != null) {
      try {
        audioRecorder.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
      audioRecorder.release();
      audioRecorder = null;
      if (save) {
        if (presenter != null) {
          presenter.onSaveMediaFile(audioPath, MEDIA_AUDIO);
        } else {
          saveMedia = true;
        }
      }
    }
  }

}
