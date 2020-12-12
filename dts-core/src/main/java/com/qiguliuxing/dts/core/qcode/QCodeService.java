package com.qiguliuxing.dts.core.qcode;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.AttributedString;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.qiguliuxing.dts.core.storage.StorageService;
import com.qiguliuxing.dts.core.system.SystemConfig;
import com.qiguliuxing.dts.db.domain.DtsGroupon;

import cn.binarywang.wx.miniapp.api.WxMaService;
import me.chanjar.weixin.common.error.WxErrorException;

@Service
public class QCodeService {
	private static final Logger logger = LoggerFactory.getLogger(QCodeService.class);
	
	@Autowired
	WxMaService wxMaService;

	@Autowired
	private StorageService storageService;

	public String createGrouponShareImage(String goodName, String goodPicUrl, DtsGroupon groupon,BigDecimal counterPrice,BigDecimal retailPrice) {
		try {
			// 创建该商品的二维码
			File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("groupon," + groupon.getId(),
					"pages/index/index");
			FileInputStream inputStream = new FileInputStream(file);
			// 将商品图片，商品名字,商城名字画到模版图中
			byte[] imageData = drawPicture(inputStream, goodPicUrl, goodName,counterPrice,retailPrice);
			ByteArrayInputStream inputStream2 = new ByteArrayInputStream(imageData);
			// 存储分享图
			String url = storageService.store(inputStream2, imageData.length, "image/jpeg",
					getKeyName(groupon.getId().toString()));

			return url;
		} catch (WxErrorException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	/**
	 * 创建商品分享图
	 *
	 * @param goodId
	 * @param goodPicUrl
	 * @param goodName
	 */
	public String createGoodShareImage(String goodId, String goodPicUrl, String goodName,BigDecimal counterPrice,BigDecimal retailPrice) {
		if (!SystemConfig.isAutoCreateShareImage())
			return "";
		try {
			// 创建该商品的二维码
			File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("goods," + goodId, "pages/index/index");
			FileInputStream inputStream = new FileInputStream(file);
			// 将商品图片，商品名字,商城名字画到模版图中
			byte[] imageData = drawPicture(inputStream, goodPicUrl,goodName,counterPrice,retailPrice);
			ByteArrayInputStream inputStream2 = new ByteArrayInputStream(imageData);
			// 存储分享图
			String url = storageService.store(inputStream2, imageData.length, "image/jpeg", getKeyName(goodId));
			logger.info("创建商品分享图 URL:{}",url);
			return url;
		} catch (WxErrorException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	private String getKeyName(String goodId) {
		return "GOOD_QCODE_" + goodId + ".jpg";
	}

	/**
	 * 将商品图片，商品名字画到模版图中
	 *
	 * @param qrCodeImg
	 *            二维码图片
	 * @param goodPicUrl
	 *            商品图片地址
	 * @param goodName
	 *            商品名称
	 * @return
	 * @throws IOException
	 */
	private byte[] drawPicture(InputStream qrCodeImg, String goodPicUrl, String goodName,BigDecimal counterPrice,BigDecimal retailPrice) throws IOException {
		// 底图
		ClassPathResource redResource = new ClassPathResource("back.png");
		BufferedImage red = ImageIO.read(redResource.getInputStream());

		// 商品图片
		URL goodPic = new URL(goodPicUrl);
		BufferedImage goodImage = ImageIO.read(goodPic);

		// 小程序二维码
		BufferedImage qrCodeImage = ImageIO.read(qrCodeImg);

		// --- 画图 ---

		// 底层空白 bufferedImage
		BufferedImage baseImage = new BufferedImage(red.getWidth(), red.getHeight(), BufferedImage.TYPE_4BYTE_ABGR_PRE);

		// 画上图片
		drawImgInImg(baseImage, red, 0, 0, red.getWidth(), red.getHeight());

		// 画上商品图片
		drawImgInImg(baseImage, goodImage, 71, 69, 660, 660);

		// 画上小程序二维码
		drawImgInImg(baseImage, qrCodeImage, 448, 767, 300, 300);

		// 写上商品名称,截取前面的12个字符长度
		if (goodName.length() > 12) {
			goodName = goodName.substring(0, 12) + "...";
		}
		Color colorComm = new Color(60, 60, 60);
		drawTextInImg(baseImage, goodName, 65, 867, colorComm, 28);

		Color priceColor = new Color(240, 20, 20);
		drawTextInImg(baseImage, "现价  ", 65, 787, colorComm, 24);
		drawTextInImg(baseImage, "￥ ", 120, 787, priceColor, 24);
		drawTextInImg(baseImage, retailPrice.toString(), 145, 787, priceColor, 34);
		drawStrikethroughTextInImg(baseImage, counterPrice.toString(), 275, 787, colorComm, 24);
		
		
		// 写上商城名称
		// drawTextInImgCenter(baseImage, shopName, 98);

		// 转jpg
		BufferedImage result = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(),
				BufferedImage.TYPE_3BYTE_BGR);
		result.getGraphics().drawImage(baseImage, 0, 0, null);
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ImageIO.write(result, "jpg", bs);

		// 最终byte数组
		return bs.toByteArray();
	}

	@SuppressWarnings("unused")
	private void drawTextInImgCenter(BufferedImage baseImage, String textToWrite, int y) {
		Graphics2D g2D = (Graphics2D) baseImage.getGraphics();
		g2D.setColor(new Color(167, 136, 69));

		String fontName = "Microsoft YaHei";

		Font f = new Font(fontName, Font.PLAIN, 28);
		g2D.setFont(f);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 计算文字长度，计算居中的x点坐标
		FontMetrics fm = g2D.getFontMetrics(f);
		int textWidth = fm.stringWidth(textToWrite);
		int widthX = (baseImage.getWidth() - textWidth) / 2;
		// 表示这段文字在图片上的位置(x,y) .第一个是你设置的内容。

		g2D.drawString(textToWrite, widthX, y);
		// 释放对象
		g2D.dispose();
	}
	
	private void drawStrikethroughTextInImg(BufferedImage baseImage, String textToWrite, int x, int y,Color color,int fontSize) {
		Graphics2D g2D = (Graphics2D) baseImage.getGraphics();
		g2D.setColor(color);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AttributedString as = new AttributedString(textToWrite); 
		// TODO 注意，这里的字体必须安装在服务器上
		//g2D.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize));
	    as.addAttribute(TextAttribute.FONT, new Font("Microsoft YaHei", Font.PLAIN, fontSize));   
	    as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON); //设置删除线
	   // as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 1, 15); //下划线
		
		g2D.drawString(as.getIterator(), x, y);
		g2D.dispose();
	}

	private void drawTextInImg(BufferedImage baseImage, String textToWrite, int x, int y,Color color,int fontSize) {
		Graphics2D g2D = (Graphics2D) baseImage.getGraphics();
		g2D.setColor(color);

		// TODO 注意，这里的字体必须安装在服务器上
		g2D.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize));
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2D.drawString(textToWrite, x, y);
		g2D.dispose();
	}

	private void drawImgInImg(BufferedImage baseImage, BufferedImage imageToWrite, int x, int y, int width,
			int heigth) {
		Graphics2D g2D = (Graphics2D) baseImage.getGraphics();
		g2D.drawImage(imageToWrite, x, y, width, heigth, null);
		g2D.dispose();
	}

	/**
	 * 根据用户创建用户的分享二维码
	 * @param userId
	 * @return
	 */
	public String createShareUserImage(Integer userId) {
		InputStream fileStream = null;
		String url = null;
		try {
			File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("user," + userId, "pages/index/index");
			FileInputStream inputStream = new FileInputStream(file);
			
			fileStream = new FileInputStream(file);
			url = storageService.store(inputStream, fileStream.available(), "image/jpeg", getShareUserKey(userId));
			logger.info("创建商品分享图 URL:{}",url);
			
		} catch (WxErrorException e) {
			logger.error("创建代理用户推荐共享二维码 错误:{}",e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("创建代理用户推荐共享二维码 错误:{}",e.getMessage());
			e.printStackTrace();
		}
		return url;
		
	}

	/**
	 * 分享代理用户二维码图片在OSS上面的名称
	 * @param userId
	 * @return
	 */
	private String getShareUserKey(Integer userId) {
		return "USER_QCODE_" + userId + ".jpg";
	}
}
