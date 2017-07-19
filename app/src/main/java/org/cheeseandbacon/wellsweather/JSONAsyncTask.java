package org.cheeseandbacon.wellsweather;

import android.os.AsyncTask;
import android.util.Log;

public class JSONAsyncTask extends
		AsyncTask<Integer, Integer, Integer> {
	private static final String TAG = "JSONAsyncTask";

	private Exception e;
	private JSONAsyncRequest request;
	private JSONAsyncResult result;

	public JSONAsyncTask(JSONAsyncRequest request){
		super();

		this.request = request;
	}
	
	@Override
	protected Integer doInBackground(Integer... params) {
		result = new JSONAsyncResult();

		JSONService service = new JSONService();

		try{
			Log.d(TAG, "Calling execute for " + request.getType());
			publishProgress(1);
			result = service.execute(request);
			Log.d(TAG, "execute complete for " + request.getType());
		} catch (Exception e) {
			Log.e(TAG, e.getClass().getName());
			this.e = e;
		}
		
		return result.getHttpStatus();
	}

	@Override
	protected void onPreExecute(){
		request.getCallBack().taskStarting(this);
	}

	@Override
	protected void onPostExecute(Integer result) {
		request.getCallBack().taskComplete(this);
	}

	public Exception getE() {
		return e;
	}

	public JSONAsyncRequest getRequest() {
		return request;
	}

	public JSONAsyncResult getResult(){
		return result;
	}

	public void setE(Exception e) {
		this.e = e;
	}

	public void setRequest(JSONAsyncRequest request) {
		this.request = request;
	}

	public JSONAsyncRequest.Type getType(){
		return request.getType();
	}
}
