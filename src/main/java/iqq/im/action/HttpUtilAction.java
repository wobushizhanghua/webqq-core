package iqq.im.action;

import iqq.im.QQActionListener;
import iqq.im.QQException;
import iqq.im.bean.QQEmail;
import iqq.im.core.QQConstants;
import iqq.im.core.QQContext;
import iqq.im.event.QQActionEvent;
import iqq.im.http.QQHttpRequest;
import iqq.im.http.QQHttpResponse;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DeleteEmailAction class.</p>
 */
public class HttpUtilAction extends AbstractHttpAction {
	private static final Logger LOG = LoggerFactory.getLogger(HttpUtilAction.class);
	private Map <String, String> params;
	private String url;

	/**
	 * <p>Constructor for DeleteEmailAction.</p>
	 *
	 * @param markList a {@link java.util.List} object.
	 * @param context a {@link iqq.im.core.QQContext} object.
	 * @param listener a {@link iqq.im.QQActionListener} object.
	 */
	public HttpUtilAction(
			QQContext context, String url, Map <String, String> params, QQActionListener listener) {
		super(context, listener);
		this.url = url;
		this.params = params;

	}
	/** {@inheritDoc} */
	@Override
	public QQHttpRequest buildRequest() throws QQException {
		QQHttpRequest req = createHttpRequest("GET", this.url);;
		for(String key : this.params.keySet()) {
			req.addGetValue(key, this.params.get(key));
		}
		return req;
	}
	
	// ({msg : "new successful",rbkey : "1391255617",status : "false"})
	/** {@inheritDoc} */
	@Override
	protected void onHttpStatusOK(QQHttpResponse response) throws QQException {
		String ct = response.getResponseString();
		LOG.info("Extra Send Back: " + ct);
		if(ct.contains("success")) {
			notifyActionEvent(QQActionEvent.Type.EVT_OK, ct);
		} else {
			notifyActionEvent(QQActionEvent.Type.EVT_ERROR, ct);
		}
	}

}
