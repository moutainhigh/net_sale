package org.llw.common.web.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.llw.common.web.identify.ImageIdentify;

public class IdentifyUtil {

	/***
	 * 获取随机码，数字加字母
	 * @param size 【4-6】
	 * @return
	 */
	public static ImageIdentify getNewIdentify(int size) {
		return IdentifyUtil.getRandomIdentify(IdentifyUtil.background, IdentifyUtil.fontColor, size);
	}
	
	/**
	 * 获取随机码，数字加字母
	 * @param size		【4-6】
	 * @param width		图片宽度
	 * @param height	图片高度
	 * @return
	 */
	public static ImageIdentify getIdentify(int size,int width,int height) {
		return IdentifyUtil.getRandomIdentify(IdentifyUtil.background, IdentifyUtil.fontColor, size,width,height);
	}
	
	/**
	 * 生成随机码，没有划线
	 * @param size
	 * @return
	 */
	public static ImageIdentify getNoLineIdentify(int size) {
		return IdentifyUtil.getRandomIdentify(IdentifyUtil.background, IdentifyUtil.fontColor, size);
	}
	
	/***
	 * 获取随机数字图片
	 * @param size 【4-6】
	 * @return
	 */
	public static ImageIdentify getIdentifyNum(int size) {
		return IdentifyUtil.getRandomNumIdentify(IdentifyUtil.background, IdentifyUtil.fontColor, size);
	}
	/**
	 * 随机验证码
	 * @param size
	 * @return
	 */
	public static ImageIdentify getRandomIdentify(Color[] background, Color[] fontColor, int size) {
		if(size<4|| size>6){
			size = 4;
		}
		ImageIdentify ideintify = new ImageIdentify();
		// 在内存中创建图象
		int width = size*25+8, height =40;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		Random random = new Random();
		// 设定背景色
		g.setColor(background[random.nextInt(2)]);
		g.fillRect(0, 0, 200, 50);
		// 设定字体
		g.setFont(new Font("Consolas", Font.CENTER_BASELINE | Font.ITALIC, 28));
		// 产生0条干扰线，
		g.drawLine(0, 0, 0, 0);
		// 取随机产生的认证码(4位数字)
		StringBuffer sRand = new StringBuffer("");
		
		// 添加噪点  
	    float yawpRate = 0.05f;// 噪声率  
        int area = (int) (yawpRate * width * height);  
        for (int i = 0; i < area; i++) {  
          random = new Random();
		  int x = random.nextInt(width);  
          int y = random.nextInt(height);  
          int rgb = getRandomIntColor();  
          image.setRGB(x, y, rgb);  
        }  
		for (int i = 0; i < size; i++) {
			String rand = getRandValue();
			sRand.append(rand);
			// 将认证码显示到图象中
			g.setColor(fontColor[i]);// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 25 * i + 6, 30);
		}
		// 图象生效
		g.dispose();
		ideintify.setImage(image);
		ideintify.setRandCode(sRand.toString());
		return ideintify;
	}
	
	public static ImageIdentify getRandomNumIdentify(Color[] background, Color[] fontColor, int size) {
		if(size<4|| size>6){
			size = 4;
		}
		ImageIdentify ideintify = new ImageIdentify();
		// 在内存中创建图象
		int width = size*25+8, height =40;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 设定背景色
		Random random = new Random();
		g.setColor(background[random.nextInt(2)]);
		g.fillRect(0, 0, 200, 50);
		// 设定字体
		g.setFont(new Font("Consolas", Font.CENTER_BASELINE | Font.ITALIC, 28));
		// 产生0条干扰线，
		g.drawLine(0, 0, 0, 0);
		// 取随机产生的认证码(4位数字)
		StringBuffer sRand = new StringBuffer("");
		
		// 添加噪点  
	    float yawpRate = 0.05f;// 噪声率  
        int area = (int) (yawpRate * width * height);  
        for (int i = 0; i < area; i++) {  
          random = new Random();
		  int x = random.nextInt(width);  
          int y = random.nextInt(height);  
          int rgb = getRandomIntColor();  
          image.setRGB(x, y, rgb);  
        }  
		for (int i = 0; i < size; i++) {
			String rand = getRandNum();
			sRand.append(rand);
			// 将认证码显示到图象中
			g.setColor(fontColor[i]);// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 25 * i + 6, 30);
		}
		// 图象生效
		g.dispose();
		ideintify.setImage(image);
		ideintify.setRandCode(sRand.toString());
		return ideintify;
	}
	
	/**
	 * 随机验证码
	 * @param size
	 * @return
	 */
	private static ImageIdentify getRandomIdentify(Color[] background, Color[] fontColor, int size,int width,int height) {
		if(size<4|| size>6){
			size = 4;
		}
		ImageIdentify ideintify = new ImageIdentify();
		// 在内存中创建图象
		if (width<=(size*18+8) || height<=28) {
			width = size*18+8; height = 28;
		}
		
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		// 获取图形上下文
		Graphics g = image.getGraphics();
		// 设定背景色
		Random random = new Random();
		g.setColor(background[random.nextInt(2)]);
		g.fillRect(0, 0, 200, 50);
		// 设定字体
		g.setFont(new Font("Consolas", Font.CENTER_BASELINE | Font.ITALIC, height-10));
		// 产生0条干扰线，
		g.drawLine(0, 0, 0, 0);
		// 取随机产生的认证码(4位数字)
		StringBuffer sRand = new StringBuffer("");
		
		// 添加噪点  
	    float yawpRate = 0.05f;// 噪声率  
        int area = (int) (yawpRate * width * height);  
        for (int i = 0; i < area; i++) {  
          random = new Random();
		  int x = random.nextInt(width);  
          int y = random.nextInt(height);  
          int rgb = getRandomIntColor();  
          image.setRGB(x, y, rgb);  
        }  
		for (int i = 0; i < size; i++) {
			String rand = getRandValue();
			sRand.append(rand);
			// 将认证码显示到图象中
			g.setColor(fontColor[i]);// 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
			g.drawString(rand, 25 * i + 6, 30);
		}
		// 图象生效
		g.dispose();
		ideintify.setImage(image);
		ideintify.setRandCode(sRand.toString());
		return ideintify;
	}
	
	
	private static int getRandomIntColor() {  
        int[] rgb = getRandomRgb();  
        int color = 0;  
        for (int c : rgb) {  
            color = color << 8;  
            color = color | c;  
        }  
        return color;  
    }  
   private static int[] getRandomRgb() {  
        int[] rgb = new int[3];  
        for (int i = 0; i < 3; i++) {  
            Random random = new Random();
			rgb[i] = random .nextInt(255);  
        }  
        return rgb;  
    } 
   
	private static Color[] background = new Color[]{new Color(190, 220, 238), new Color(241, 239, 215)};
	private static Color[] fontColor = new Color[]{new Color(27, 128, 86),new Color(67, 56, 153),new Color(201, 145, 49), new Color(114, 66, 132)};
	
	private static List<String> listRands = new ArrayList<String>();
	private static List<String> listNumber = new ArrayList<String>();
	static {
		listNumber.add("0");
		listNumber.add("1");
		listNumber.add("2");
		listNumber.add("3");
		listNumber.add("4");
		listNumber.add("5");
		listNumber.add("6");
		listNumber.add("7");
		listNumber.add("8");
		listNumber.add("9");
		
		listRands.add("0");
		listRands.add("1");
		listRands.add("2");
		listRands.add("3");
		listRands.add("4");
		listRands.add("5");
		listRands.add("6");
		listRands.add("7");
		listRands.add("8");
		listRands.add("9");
		listRands.add("A");
		listRands.add("C");
		listRands.add("D");
		listRands.add("E");
		listRands.add("F");
		listRands.add("G");
		listRands.add("H");
		listRands.add("J");
		listRands.add("K");
		listRands.add("L");
		listRands.add("M");
		listRands.add("N");
		listRands.add("P");
		listRands.add("Q");
		listRands.add("R");
		listRands.add("S");
		listRands.add("T");
		listRands.add("U");
		listRands.add("V");
		listRands.add("W");
		listRands.add("X");
		listRands.add("Y");
		listRands.add("Z");
	}
	
	private static String getRandValue() {
		Random random = new Random();
		String rand = listRands.get(random.nextInt(listRands.size()));
		return rand;
	}
	private static String getRandNum() {
		Random random = new Random();
		String rand = listNumber.get(random.nextInt(listNumber.size()));
		return rand;
	}
	/**
	 * 获取几位随机数字
	 * @param size 【4-6】
	 * @return
	 */
	public static String getRandNum(int size) {
		if(size<4|| size>6){
			size = 4;
		}
		// 取随机产生的认证码(4位数字)
		StringBuffer sRand = new StringBuffer("");
		for (int i = 0; i < size; i++) {
			String rand = getRandNum();
			sRand.append(rand);
		}
		return sRand.toString();
	}
	
	/**
	 * 获取几位随机数字和字母
	 * @param size 【4-8】
	 * @return
	 */
	public static String getRandValue(int size) {
		if(size<4|| size>8){
			size = 6;
		}
		// 取随机产生的认证码(4位数字)
		StringBuffer sRand = new StringBuffer("");
		for (int i = 0; i < size; i++) {
			String rand = getRandValue();
			sRand.append(rand);
		}
		return sRand.toString();
	}
	
	/**
	 * 给定范围获得随机颜色
	 * @param fc
	 * @param bc
	 * @return
	 */
	public static Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}
}
