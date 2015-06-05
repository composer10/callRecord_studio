package kr.co.composer.callrecord.page;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import kr.co.composer.callrecord.R;
import kr.co.composer.callrecord.bo.Call;
import kr.co.composer.callrecord.callhistory.HistoryAdapter;
import kr.co.composer.callrecord.dao.record.CallDAO;
import kr.co.composer.callrecord.recorder.AudioRecorder;
import kr.co.composer.callrecord.sharedpref.ConfigPreferenceManager;

public class CallHistoryActivity extends Activity {

	CallDAO callDAO;

	ListView listView;
	
	HistoryAdapter historyAdapter;

	SparseBooleanArray selected;
	
	ArrayList<Call> callList;

	AudioRecorder audioRecorder;
	
	File playFileName;
	
	String filePath;
	
	ConfigPreferenceManager configPreferenceManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_history_activity);

		callDAO = new CallDAO(getApplicationContext());

		configPreferenceManager = ConfigPreferenceManager.getInstance();
		audioRecorder = new AudioRecorder();
//		액션바 타이틀변경
		ActionBar ab = this.getActionBar();
		ab.setTitle("녹음목록");
//		액션바 백버튼 생성
		ab.setDisplayHomeAsUpEnabled(true);
		
		listView = (ListView)findViewById(R.id.call_history_listview);
		
		callDAO = new CallDAO(getApplicationContext());

		Cursor cursor = callDAO.select();
		callList = callDAO.cursorCallList(cursor);
		historyAdapter = new HistoryAdapter(CallHistoryActivity.this, R.layout.call_history_item, callList);
		
		listView.setAdapter(historyAdapter);
		listView.setDivider(null);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				// Capture total checked items
				final int checkedCount = listView.getCheckedItemCount();
				// Set the CAB title according to total checked items
				mode.setTitle(checkedCount + " Selected");
				// Calls toggleSelection method from ListViewAdapter Class
//				Log.i("포지션확인",String.valueOf(position));
//				Log.i("아이템확인",String.valueOf(id));
				historyAdapter.toggleSelection(position);
			}
			
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.deleteAction:
					// Calls getSelectedIds method from ListViewAdapter Class
					selected = historyAdapter
							.getSelectedIds();
					// Captures all selected ids with a loop
					for (int i = (selected.size() - 1); i >= 0; i--) {
						if (selected.valueAt(i)) {
							Call selecteditem = historyAdapter
									.getItem(selected.keyAt(i));
							// Remove selected items following the ids
							historyAdapter.remove(selecteditem);
						}
					}
					// Close CAB
					mode.finish();
					return true;
				case R.id.allAction:
					historyAdapter.toggleAll(listView);
					Toast.makeText(getBaseContext(), "전체선택", Toast.LENGTH_SHORT).show();
					return true;
				default:
					return false;
				}
			}
			
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(R.menu.history_action, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// TODO Auto-generated method stub
				historyAdapter.removeSelection();
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		
		
		
		
		
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long positionL) {
					playFileName = callDAO.selectFile(view.getId());
					Log.i("audioFileName", playFileName.toString());
					
//					Intent audioStartIntent = new Intent(android.content.Intent.ACTION_VIEW);
					MediaPlayer player = new MediaPlayer();
					try {
						player.setDataSource(playFileName.toString());
						player.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					player.start();
					
					
//				    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && isMediaDocument(Uri.fromFile(playFileName)))
//				    {
//				        final String docId = DocumentsContract.getDocumentId(Uri.fromFile(playFileName));
//				        System.out.println("스트링확인: "+docId);
//				        final String[] split = docId.split(":");
//				        final String type = split[0];
//
//				        Uri contentUri = null;
//				        if ("audio".equals(type)) 
//				        {
//				            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//				        }
//
//				        final String selection = "_id=?";
//				        final String[] selectionArgs = new String[] {
//				                split[1]
//				        };
//
//				        filePath = getDataColumn(getBaseContext(), contentUri, selection, selectionArgs);
//				    }
//				    System.out.println("파일패스확인 : "+filePath);
//					audioStartIntent.setDataAndType(Uri.parse(filePath), "audio/*");
					
//					audioStartIntent.setDataAndType(Uri.fromFile(playFileName), "audio/*");
//					CallHistoryActivity.this.startActivity(audioStartIntent);
					}
		});
	}
	
	//롤리팝 이후 파일
	private static boolean isMediaDocument(Uri uri)
	{
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
	{
	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
	        if (cursor != null && cursor.moveToFirst())
	        {
	            final int column_index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(column_index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			CallHistoryActivity.this.finish();
		
		}
		return super.onOptionsItemSelected(item);
	}
	
}

