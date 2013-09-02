package lemon.weixin.biz.parser;

import com.thoughtworks.xstream.XStream;

import lemon.shared.common.Message;
import lemon.weixin.bean.message.VideoMessage;
import lemon.weixin.util.WXHelper;

/**
 * A video message parser
 * 
 * @author lemon
 * 
 */
public final class VideoMsgParser extends WXMsgParser {
	private XStream xStream = WXHelper.createXstream();

	@Override
	public final VideoMessage toMsg(String msg) {
		xStream.processAnnotations(VideoMessage.class);
		return (VideoMessage) xStream.fromXML(msg);
	}

	@Override
	public final String toXML(Message rMsg) {
		xStream.processAnnotations(VideoMessage.class);
		return xStream.toXML(rMsg);
	}
}