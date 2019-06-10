package com.kaarss.fatalk;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private byte[] _content;
    private UploadCallbacks _listener;
    private int _uploadType; // 1:media; 2:preview; 3:user
    private String _uploadId;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    public interface UploadCallbacks {
        void onProgressUpdate(String uploadId, int uploadType, int percentage);
    }

    public ProgressRequestBody(final byte[] content,
                               final  UploadCallbacks listener,
                               final String uploadId,
                               final int uploadType) {
        _content = content;
        _listener = listener;
        _uploadId = uploadId;
        _uploadType = uploadType;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse("multipart/form-data");
    }

    @Override
    public long contentLength() throws IOException {
        return _content.length;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = _content.length;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (InputStream in = new ByteArrayInputStream(_content)) {
            long uploaded = 0;
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // update progress on UI thread
                handler.post(new ProgressUpdater(_uploadId,_uploadType, uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private String mUploadId;
        private int mUploadType;
        private long mUploaded;
        private long mTotal;
        public ProgressUpdater(String uploadId, int uploadType, long uploaded, long total) {
            mUploadId = uploadId;
            mUploadType = uploadType;
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            _listener.onProgressUpdate(mUploadId,mUploadType,(int)(100 * mUploaded / mTotal));
        }
    }
}
