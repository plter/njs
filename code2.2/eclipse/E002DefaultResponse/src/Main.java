import com.plter.pws.http.DynamicWebResponse;


public class Main extends DynamicWebResponse {

	@Override
	protected void handle(IWriteCompleteListener completer,
			IWriteErrorListener writeError) {
		write("This is index", completer);
	}

}
