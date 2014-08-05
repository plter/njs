import com.plter.pws.http.DynamicWebResponse;


public class Echo extends DynamicWebResponse {

	@Override
	protected void handle(IWriteCompleteListener completer,
			IWriteErrorListener writeError) {
		
		for (int i = 0; i < 100; i++) {
			write(String.valueOf(i).concat("<br>"));
		}
		
		completer.completed();
	}

}
