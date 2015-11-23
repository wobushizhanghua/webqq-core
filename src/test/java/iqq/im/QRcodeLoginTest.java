package iqq.im;

import iqq.im.actor.ThreadActorDispatcher;
import iqq.im.bean.QQDiscuz;
import iqq.im.bean.QQGroup;
import iqq.im.bean.QQMsg;
import iqq.im.bean.QQUser;
import iqq.im.bean.content.FaceItem;
import iqq.im.bean.content.FontItem;
import iqq.im.bean.content.TextItem;
import iqq.im.event.QQActionEvent;
import iqq.im.event.QQNotifyEvent;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用二维码登录WebQQ
 * <p/>
 * <p/>
 * Created by Tony on 10/6/15.
 */
public class QRcodeLoginTest {

    static WebQQClient mClient = new WebQQClient(new QQNotifyListener() {
        @Override
        public void onNotifyEvent(QQNotifyEvent event) {
            System.out.println(event);
            switch (event.getType()) {
                case CHAT_MSG:
                    QQMsg revMsg = (QQMsg) event.getTarget();
                    revMsg(revMsg);
                    break;
            }
        }
    }, new ThreadActorDispatcher());

    public static void main(String[] args) {
        // 获取二维码
        mClient.getQRcode(new QQActionListener() {
            @Override
            public void onActionEvent(QQActionEvent event) {
                if (event.getType() == QQActionEvent.Type.EVT_OK) {
                    try {
                        BufferedImage verify = (BufferedImage) event.getTarget();
                        ImageIO.write(verify, "png", new File("qrcode.png"));
                        System.out.println("请扫描在项目根目录下qrcode.png图片");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("获取二维码失败");
                }
            }
        });
        // 检查二维码状态
        mClient.checkQRCode(new QQActionListener() {
            @Override
            public void onActionEvent(QQActionEvent event) {
                System.out.println("checkQRCode " + event);
                switch (event.getType()) {
                    case EVT_OK:
                        // 扫描通过,登录成功
                        mClient.beginPollMsg();
                        break;
                    case EVT_ERROR:
                        QQException ex = (QQException) (event.getTarget());
                        QQException.QQErrorCode code = ex.getError();
                        switch (code) {
                            // 二维码有效,等待用户扫描
                            // 二维码已经扫描,等用户允许登录
                            case QRCODE_OK:
                            case QRCODE_AUTH:
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                // 继续检查二维码状态
                                mClient.checkQRCode(this);
                                break;
                        }
                        break;
                }
            }
        });
    }

    private static void revMsg(QQMsg revMsg) {
    	
        switch (revMsg.getType()) {
            case BUDDY_MSG:
            case SESSION_MSG:
                sendMsg(revMsg,revMsg.getFrom());
                break;
            case GROUP_MSG:
                sendMsg(revMsg, revMsg.getGroup());
                break;
            case DISCUZ_MSG:
                sendDiscuz(revMsg,revMsg.getDiscuz());
        }
    }

    public static void sendMsg(final QQMsg revMsg, final QQUser user) {
        System.out.println("sendMsg " + user);
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "buddy");
        params.put("nickname", user.getNickname());
        params.put("qq", Long.toString(user.getQQ()));
        params.put("msg", revMsg.getText());
        mClient.httpGet("http://192.168.2.122/webqq/recv.php", params, new QQActionListener() {
			
			@Override
			public void onActionEvent(QQActionEvent event) {
				// TODO Auto-generated method stub
				System.out.println(event.getType());
				System.out.println(event.getTarget());
				if (!event.getType().equals(QQActionEvent.Type.EVT_OK)) {
					return;
				}
				try {
					String response = event.getTarget().toString();
					System.out.println(response);
					JSONObject obj = new JSONObject(response);
					if (obj.getString("cmd").equals("send_buddy")) {
				        // 组装QQ消息发送回去
				        QQMsg sendMsg = new QQMsg();
				        sendMsg.setTo(user);                                // QQ好友UIN
				        sendMsg.setType(revMsg.getType());              // 发送类型为好友
				        // QQ内容
				        sendMsg.addContentItem(new TextItem(obj.getString("msg"))); // 添加文本内容
				        sendMsg.addContentItem(new FaceItem(74));           // QQ id为0的表情
				        sendMsg.addContentItem(new FontItem());             // 使用默认字体
				        mClient.sendMsg(sendMsg, null);                     // 调用接口发送消息
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    }

    public static void sendMsg(final QQMsg revMsg, final QQGroup group) {
        System.out.println("sendMsg " + group);
        Map<String, String> params = new HashMap<String, String>();
        final QQUser user = revMsg.getFrom();
        params.put("type", "buddy");
        params.put("nickname", user.getNickname());
        params.put("qq", Long.toString(user.getQQ()));
        params.put("msg", revMsg.getText());
        mClient.httpGet("http://192.168.2.122/webqq/recv.php", params, new QQActionListener() {
			
			@Override
			public void onActionEvent(QQActionEvent event) {
				// TODO Auto-generated method stub
				System.out.println(event.getType());
				System.out.println(event.getTarget());
				if (!event.getType().equals(QQActionEvent.Type.EVT_OK)) {
					return;
				}
				try {
					String response = event.getTarget().toString();
					System.out.println(response);
					JSONObject obj = new JSONObject(response);
					if (obj.getString("cmd").equals("send_buddy")) {
				        // 组装QQ消息发送回去
				        QQMsg sendMsg = new QQMsg();
				        sendMsg.setGroup(group);                                // QQ好友UIN
				        sendMsg.setType(revMsg.getType());              // 发送类型为好友
				        // QQ内容
				        sendMsg.addContentItem(new TextItem(obj.getString("msg"))); // 添加文本内容
//				        sendMsg.addContentItem(new FaceItem(74));           // QQ id为0的表情
				        sendMsg.addContentItem(new FontItem());             // 使用默认字体
				        mClient.sendMsg(sendMsg, null);                     // 调用接口发送消息
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    }

    public static void sendDiscuz(QQMsg revMsg, QQDiscuz discuz) {
        System.out.println("sendMsg " + discuz);

        // 组装QQ消息发送回去
        QQMsg sendMsg = new QQMsg();
        sendMsg.setDiscuz(discuz);                                // QQ好友UIN
        sendMsg.setType(QQMsg.Type.DISCUZ_MSG);              // 发送类型为好友
        // QQ内容
        sendMsg.addContentItem(new TextItem("hello world")); // 添加文本内容
        sendMsg.addContentItem(new FaceItem(74));           // QQ id为0的表情
        sendMsg.addContentItem(new FontItem());             // 使用默认字体
        mClient.sendMsg(sendMsg, null);                     // 调用接口发送消息
    }
}
