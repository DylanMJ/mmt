package lemon.yixin.message.bean;

import lemon.shared.toolkit.xstream.annotations.XStreamCDATA;
import lemon.shared.toolkit.xstream.annotations.XStreamProcessCDATA;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Text message
 * 
 * @author lemon
 * @version 1.0
 * 
 */
@XStreamAlias("xml")
@XStreamProcessCDATA
public class TextMessage extends YiXinMessage {
	/** Content */
	@XStreamAlias("Content")
	@XStreamCDATA
	private String content;

	public TextMessage(){
		super(MsgType.TEXT);
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
