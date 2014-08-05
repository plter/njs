import com.plter.pws.http.DynamicWebResponse;


public class HelloPWS extends DynamicWebResponse {

	@Override
	protected void handle(IWriteCompleteListener completer,
			IWriteErrorListener writeError) {
		write("Hello PWS!",completer);
	}
}
