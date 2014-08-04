import com.plter.pws.http.DynamicWebResponse;


public class Hello extends DynamicWebResponse {

	@Override
	protected void handle(IWriteCompleteListener completer,
			IWriteErrorListener writeError) {
		write("Hello Client!", completer);
	}

}
