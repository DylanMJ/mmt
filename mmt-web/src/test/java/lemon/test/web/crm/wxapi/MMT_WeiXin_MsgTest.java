package lemon.test.web.crm.wxapi;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lemon.shared.api.MmtAPI;
import lemon.shared.customer.bean.Customer;
import lemon.shared.customer.mapper.CustomerMapper;
import lemon.shared.entity.Status;
import lemon.weixin.config.WeiXin;
import lemon.weixin.config.bean.WeiXinConfig;
import lemon.weixin.config.mapper.WXConfigMapper;
import lemon.weixin.message.WeiXinMsgHelper;
import lemon.weixin.message.bean.MusicMessage;
import lemon.weixin.message.bean.NewsMessage;
import lemon.weixin.message.bean.TextMessage;
import lemon.weixin.message.parser.MusicMsgParser;
import lemon.weixin.message.parser.NewsMsgParser;
import lemon.weixin.message.parser.TextMsgParser;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(JUnit4.class)
public class MMT_WeiXin_MsgTest {
	private MmtAPI api;
	private final String Subscribe_msg = "Welcome to Subscribe Lemon Test.";
	private final String Welcome_msg = "Welcome to Subscribe Lemon Test.";
	private final String TOKEN = "1230!)*!)*#)!*Q)@)!*";
	private final String MMT_TOKEN = "lemonxoewfnvowensofcewniasdmfo";
	private final String bizClass = "lemon.web.crm.wxapi.MMT_WeiXin_MsgProcessor";
	private final int cust_id = 201;
	private WeiXinMsgHelper msgHelper;
	private ApplicationContext acx;
	private CustomerMapper customerMapper;
	private WXConfigMapper	wxConfigMapper;
	@Before
	public void init() {
		String[] resource = { "classpath:spring-db.xml",
				"classpath:spring-dao.xml", "classpath:spring-service.xml" };
		acx = new ClassPathXmlApplicationContext(resource);
		api = (MmtAPI) acx.getBean("weiXinAPI");
		msgHelper = acx.getBean(WeiXinMsgHelper.class);
		customerMapper = acx.getBean(CustomerMapper.class);
		wxConfigMapper = acx.getBean(WXConfigMapper.class);
		assertNotNull(api);
		assertNotNull(msgHelper);
		assertNotNull(customerMapper);
		assertNotNull(wxConfigMapper);
		
		//add customer
		Customer cust = customerMapper.getCustomer(cust_id);
		if(cust == null){
			cust = new Customer();
			cust.setCust_id(cust_id);
			cust.setCust_name("Test");
			cust.setMemo("");
			cust.setStatus(Status.AVAILABLE);
			customerMapper.addCustomer(cust);
			assertNotEquals(cust.getCust_id(), 0);
		}
		
		//add WeiXin configure
		WeiXinConfig cfg = wxConfigMapper.get(cust_id);
		if(null == cfg){
			cfg = new WeiXinConfig();
			cfg.setCust_id(cust_id);
			cfg.setToken(TOKEN);
			cfg.setApi_url(MMT_TOKEN);
			cfg.setWx_account("lemon_test");
			cfg.setAppid("");
			cfg.setSecret("");
			cfg.setBiz_class(bizClass);
			cfg.setSubscribe_msg(Subscribe_msg);
			cfg.setWelcome_msg(Welcome_msg);
			wxConfigMapper.save(cfg);
			assertNotEquals(cfg.getCust_id(), 0);
		}
		WeiXin.init();
		WeiXin.setConfig(cfg);
	}
	
	@Test
	public void parserMsgType() throws JDOMException, IOException{
		String msg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377241649729</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[hello,weixin, I am lemon.]]></Content></xml>";
		InputStream is = new ByteArrayInputStream(msg.getBytes("UTF-8"));
		Document doc = new SAXBuilder().build(is);
		Element msgType = doc.getRootElement().getChild("MsgType");
		Assert.assertTrue("text".equals(msgType.getValue()));
	}
	@Test
	public void textMsgTest(){
		String txtMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377241649729</CreateTime><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[hello,weixin, I am lemon.]]></Content></xml>";
		String result = api.processMsg(MMT_TOKEN, txtMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "Welcome to Subscribe Lemon Test.");
	}
	@Test
	public void subscribeTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377682037695</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[subscribe]]></Event><EventKey><![CDATA[0dfsafkqwnriksdk]]></EventKey></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), Subscribe_msg);
	}
	
	@Test
	public void unsubscribe(){
		String recvMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377682037695</CreateTime><MsgType><![CDATA[event]]></MsgType><Event><![CDATA[unsubscribe]]></Event><EventKey><![CDATA[0dfsafkqwnriksdk]]></EventKey></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		assertNull(result);
	}
	@Test
	public void linkMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377753855909</CreateTime><MsgType><![CDATA[link]]></MsgType><MsgId>1024102410241024</MsgId><Title><![CDATA[Link \"TEST\" Title]]></Title><Description><![CDATA[Link DESC]]></Description><Url><![CDATA[http://www.163.com/s/a/d/f/a]]></Url></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "MMTChat Link message replay.");
	}
	
	@Test
	public void imageMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[gh_de370ad657cf]]></ToUserName><FromUserName><![CDATA[ot9x4jpm4x_rBrqacQ8hzikL9D-M]]></FromUserName><CreateTime>1378027514</CreateTime><MsgType><![CDATA[image]]></MsgType><PicUrl><![CDATA[http://mmsns.qpic.cn/mmsns/QXd6JDcZQ1ls9utpyRLS49qltXnkjkg3DOcQSI8CO1NxptcHC16yhQ/0]]></PicUrl><MsgId>5918583105618182187</MsgId><MediaId><![CDATA[7scBMzahwP7VG0exqbE4PwDhmu87f3jiYCdOueP0gpzghvrAugPxKHvMYxTLjQqX]]></MediaId></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "<a href='http://mmsns.qpic.cn/mmsns/QXd6JDcZQ1ls9utpyRLS49qltXnkjkg3DOcQSI8CO1NxptcHC16yhQ/0'>下载图片</a>");
	}
	
	@Test
	public void locationMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[weixin]]></ToUserName><FromUserName><![CDATA[lemon]]></FromUserName><CreateTime>1377754299991</CreateTime><MsgType><![CDATA[location]]></MsgType><MsgId>1024102410241024</MsgId><Location__X>23.134521</Location__X><Location__Y>113.358803</Location__Y><Scale>20</Scale><Label><![CDATA[I am here.<xml>\"sdf\"</xml>]]></Label></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		MusicMessage msg = acx.getBean(MusicMsgParser.class).toMsg(result);
		assertEquals(msg.getMusicUrl(), "MMTChat nusic URL");
		assertEquals(msg.getHqMusicUrl(), "MMTChat HQ music URL");
	}
	@Test
	public void voiceMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[gh_de370ad657cf]]></ToUserName><FromUserName><![CDATA[ot9x4jpm4x_rBrqacQ8hzikL9D-M]]></FromUserName><CreateTime>1378193706</CreateTime><MsgType><![CDATA[voice]]></MsgType><MediaId><![CDATA[fLHx02T1fxxHyN1j2C1xiDnjklwpEYb3EyvkxykCeQ1VAlqpvepM-l4jOIKYkIo4]]></MediaId><Format><![CDATA[amr]]></Format><MsgId>5919296894823039086</MsgId><Recognition><![CDATA[]]></Recognition></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		NewsMessage msg = acx.getBean(NewsMsgParser.class).toMsg(result);
		assertEquals(msg.getArticleCount(), 2);
	}
	
	@Test
	public void videoMsgTest(){
		String recvMsg = "<xml><ToUserName><![CDATA[gh_de370ad657cf]]></ToUserName><FromUserName><![CDATA[ot9x4jpm4x_rBrqacQ8hzikL9D-M]]></FromUserName><CreateTime>1378194082</CreateTime><MsgType><![CDATA[video]]></MsgType><MediaId><![CDATA[1BZrnnbpR-Es-kuOzWbWKCpuWonEy-5r7PrZd4lliGeqwumf-ik7obib7eiALxWc]]></MediaId><ThumbMediaId><![CDATA[DeuiUHn9EW8ETn10s1BCnDM8ScTuixsMMTjaNWtIKJzJPS6Xz92VXVGUREeu89yp]]></ThumbMediaId><MsgId>5919298509730742383</MsgId></xml>";
		String result = api.processMsg(MMT_TOKEN, recvMsg);
		TextMessage msg = acx.getBean(TextMsgParser.class).toMsg(result);
		assertEquals(msg.getContent(), "You send me a video, thanks!");
	}
}
