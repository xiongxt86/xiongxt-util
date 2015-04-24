package core;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
/**
 * 图片压缩
 * @author xiongxt
 *
 */
//缺陷：本身是小图，并且以_logo结尾的，没法用默认的原图_logo来指定缩约图，可以判断后使用原图路径
public class ImageUtil {
	/**
	 * 最大宽度
	 */
	public static final int MAX_WIDTH = 40;
	/**
	 * 最大高度
	 */
	public static final int MAX_HEIGHT = 40;
	/**
	 * 图片缩约图质量
	 */
	public static final double SUPPORT = 3.0;
	/**
	 * 圆周率
	 */
	public static final double PI = 3.14159265358978;
	
	private static Logger log = LoggerFactory.getLogger(ImageUtil.class);
	
	/**
	 * 大图压缩成小图
	 * @param generate
	 * 			增加的路径 root+saveFolder(如：d:/nginx/html/cms/banner/)
	 * @param inputImage
	 *            原图片
	 * @param saveFolder
	 * 			    保存父文件夹（如：d:/nginx/html/cms/banner/123432432/）
	 * @param name
	 *            原文件名称（如：22.png）
	 * @param target
	 *            缩约图名称（如：22_logo.png）
	 *            
	 * @return 缩约图路径（如123432432/22_logo.jpg）
	 */
	public static String createThumbnail(String generate, InputStream inputImage,
			String saveFolder, String name,String target) {
		if (inputImage == null)
			throw new RuntimeException("图片不存在！");
		//统一保存路径的分隔符为"/"
		saveFolder = FileUtil.addBackslash(saveFolder);
		/**
		 * 创建目录
		 */
		File folder = new File(saveFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		//缩约图路径
		String path = saveFolder+target;
		String path2 = saveFolder+name;
		//返回结果
		String result = path.replace(generate, "");
		String result2 = path2.replace(generate, "");
		File targ = new File (path);
		//已经存在logo的图片
		if(targ.exists()){
			log.info("缩约图路径"+result+"！");
			return result;
		}
		
		BufferedImage bufferedImage = null;
		BufferedImage pbOut = null;
		BufferedImage pbFinalOut = null;
		
			try {
				bufferedImage = ImageIO.read(inputImage);
				// 读取图片大小
				int[] size = size(bufferedImage);
	
				// 不做放大操作
				if (isSmall(size[0], size[1])) {
					String prefix = FileUtil.prefix(name);
					if(!prefix.endsWith("_logo")){//直接拷贝原图一份
						log.info("缩约图路径"+result+"！");
						FileUtil.copyInputStreamToFile(inputImage, targ);
						return result;
					} else {//本身是logo图片
						log.info("缩约图路径"+result2+"！");
						return result2;
					}
				}else{
	
					int[] finalSize = finalSize(size[0], size[1]);
		
					int finalWidth = finalSize[0];
					int finalHeight = finalSize[1];
		
					int nHalfDots = (int) ((double) size[0] * SUPPORT / (double) finalWidth);
					int nDots = nHalfDots * 2 + 1;
					double[] contrib = new double[nDots];
					double[] normContrib = new double[nDots];
					double[] tmpContrib = new double[nDots];
		
					CalContrib(size, finalWidth, nHalfDots, nDots, contrib, normContrib);
					/**
					 * 水平压缩
					 */
					pbOut = HorizontalFiltering(bufferedImage, size, finalWidth,
							nHalfDots, nDots, contrib, tmpContrib, normContrib);
					/**
					 * 更新图片大小
					 */
					size = size(pbOut);
					/**
					 * 垂直压缩
					 */
					pbFinalOut = VerticalFiltering(pbOut, size, finalHeight, nHalfDots,
							nDots, contrib, tmpContrib, normContrib);
					
					
					File output = new File(folder, target);
					ImageIO.write(pbFinalOut, "JPEG", output);
					log.info("文件"+target+"已生成！");
					log.info("缩约图路径"+result+"！");
					return result;
			}
		} catch (Exception e) {
			throw new RuntimeException("读取图片失败！");
		} finally {
			if(bufferedImage != null){
				bufferedImage.flush();
				bufferedImage = null;
			}
			if(pbOut != null){
				pbOut.flush();
				pbOut = null;
			}
			if(pbFinalOut != null){
				pbFinalOut.flush();
				pbFinalOut = null;
			}
		}
	}

	/**
	 * 大图压缩成小图
	 * 
	 * @param sourceFile
	 *            原图片
	 * @param saveFolder
	 * 			    保存路径（如：D:\\nginx/html\\cms/links）
	 * @param columnName
	 *            列名（如：logo）
	 * @param columnValue
	 *            列值（如：01151600029501/1.jpg）
	 * @return 缩约图路径（如01151600029501/1_logo.jpg）
	 */
	public static String createThumbnail(String generate, File sourceFile, String saveFolder,
			String name, String target) {
		FileInputStream input = null;
		try {
			if (sourceFile == null || sourceFile.length() == 0)
				throw new RuntimeException("图片不存在！");
			input = new FileInputStream(sourceFile);
			return createThumbnail(generate, input, saveFolder, name, target);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("读取图片失败！");
		} finally {
			FileUtil.release(input, null);
		}

	}

	/**
	 * 大图压缩成小图
	 * 
	 * @param sourcePath
	 *            原图片绝对路径
	 * @param saveFolder
	 * 			    保存路径（如：D:\\nginx/html\\cms/links）
	 * @param columnName
	 *            列名（如：logo）
	 * @param columnValue
	 *            列值（如：01151600029501/1.jpg）
	 * @return 缩约图路径（如01151600029501/1_logo.jpg）
	 */
	public static String createThumbnail(String generate, String sourcePath,String saveFolder, String name,String target) {
		if (sourcePath == null)
			throw new RuntimeException("图片不存在！");
		return createThumbnail(generate, new File(sourcePath),saveFolder, name, target);
	}

	/**
	 * 图片的宽高
	 * 
	 * @param bufferedImage
	 *            缓存的图片
	 * @return int[0] 宽 int[1] 高
	 */
	private static int[] size(Image bufferedImage) {
		int[] result = new int[2];
		result[0] = bufferedImage.getWidth(null);
		result[1] = bufferedImage.getHeight(null);
		return result;
	}

	/**
	 * 判断文件是不是尺寸合适
	 * 
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 * @return true：大小合适，使用原图
	 */
	private static boolean isSmall(int width, int height) {
		if (MAX_HEIGHT >= height && MAX_WIDTH >= width) {
			return true;
		}
		return false;
	}

	/**
	 * 缩约图尺寸
	 * 
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 * @return
	 */
	private static int[] finalSize(int width, int heigth) {
		int[] result = new int[2];
		double scaleWidth = (double) MAX_WIDTH / width;// 0.2
		double scaleHeigth = (double) MAX_HEIGHT / heigth;// 0.1
		if (scaleWidth >= scaleHeigth) {
			result[0] = (int) (width * scaleHeigth);
			result[1] = (int) (heigth * scaleHeigth);
		} else {
			result[0] = (int) (width * scaleWidth);
			result[1] = (int) (heigth * scaleWidth);
		}
		return result;
	}

	private static void CalContrib(int[] size, int finalWidth, int nHalfDots,
			int nDots, double[] contrib, double[] normContrib) {

		int center = nHalfDots;
		contrib[center] = 1.0;

		double weight = 0.0;
		int i = 0;
		for (i = 1; i <= center; i++) {
			contrib[center + i] = Lanczos(i, size[0], finalWidth);
			weight += contrib[center + i];
		}

		for (i = center - 1; i >= 0; i--) {
			contrib[i] = contrib[center * 2 - i];
		}

		weight = weight * 2 + 1.0;

		for (i = 0; i <= center; i++) {
			normContrib[i] = contrib[i] / weight;
		}

		for (i = center + 1; i < nDots; i++) {
			normContrib[i] = normContrib[center * 2 - i];
		}
	}

	// 处理边缘
	private static void CalTempContrib(int start, int stop, double[] contrib,
			double[] tmpContrib) {
		double weight = 0;

		int i = 0;
		for (i = start; i <= stop; i++) {
			weight += contrib[i];
		}

		for (i = start; i <= stop; i++) {
			tmpContrib[i] = contrib[i] / weight;
		}

	}

	private static double Lanczos(int i, int width, int finalWidth) {
		double x;
		x = (double) i * (double) finalWidth / (double) width;
		return Math.sin(x * PI) / (x * PI) * Math.sin(x * PI / SUPPORT)
				/ (x * PI / SUPPORT);
	}

	/*
	 * 图片水平滤波
	 */
	private static BufferedImage HorizontalFiltering(
			BufferedImage bufferedImage, int[] size, int iOutW, int nHalfDots,
			int nDots, double[] contrib, double[] tmpContrib,
			double[] normContrib) {
		int dwInW = size[0];
		int dwInH = size[1];
		int value = 0;
		BufferedImage pbOut = new BufferedImage(iOutW, dwInH,
				BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < iOutW; x++) {

			int startX;
			int start;
			int X = (int) (((double) x) * ((double) dwInW) / ((double) iOutW) + 0.5);
			int y = 0;

			startX = X - nHalfDots;
			if (startX < 0) {
				startX = 0;
				start = nHalfDots - X;
			} else {
				start = 0;
			}

			int stop;
			int stopX = X + nHalfDots;
			if (stopX > (dwInW - 1)) {
				stopX = dwInW - 1;
				stop = nHalfDots + (dwInW - 1 - X);
			} else {
				stop = nHalfDots * 2;
			}

			if (start > 0 || stop < nDots - 1) {
				CalTempContrib(start, stop, contrib, tmpContrib);
				for (y = 0; y < dwInH; y++) {
					value = HorizontalFilter(bufferedImage, startX, stopX,
							start, stop, y, tmpContrib);
					pbOut.setRGB(x, y, value);
				}
			} else {
				for (y = 0; y < dwInH; y++) {
					value = HorizontalFilter(bufferedImage, startX, stopX,
							start, stop, y, normContrib);
					pbOut.setRGB(x, y, value);
				}
			}
		}

		return pbOut;

	}

	// 行水平滤波
	private static int HorizontalFilter(BufferedImage bufImg, int startX,
			int stopX, int start, int stop, int y, double[] pContrib) {
		double valueRed = 0.0;
		double valueGreen = 0.0;
		double valueBlue = 0.0;
		int valueRGB = 0;
		int i, j;

		for (i = startX, j = start; i <= stopX; i++, j++) {
			valueRGB = bufImg.getRGB(i, y);

			valueRed += GetRedValue(valueRGB) * pContrib[j];
			valueGreen += GetGreenValue(valueRGB) * pContrib[j];
			valueBlue += GetBlueValue(valueRGB) * pContrib[j];
		}

		valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
				Clip((int) valueBlue));
		return valueRGB;

	}

	/*
	 * 图片垂直过滤
	 */
	private static BufferedImage VerticalFiltering(BufferedImage pbImage,
			int[] size, int iOutH, int nHalfDots, int nDots, double[] contrib,
			double[] tmpContrib, double[] normContrib) {
		int iW = size[0];
		int iH = size[1];
		int value = 0;
		BufferedImage pbOut = new BufferedImage(iW, iOutH,
				BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < iOutH; y++) {

			int startY;
			int start;
			int Y = (int) (((double) y) * ((double) iH) / ((double) iOutH) + 0.5);

			startY = Y - nHalfDots;
			if (startY < 0) {
				startY = 0;
				start = nHalfDots - Y;
			} else {
				start = 0;
			}

			int stop;
			int stopY = Y + nHalfDots;
			if (stopY > (int) (iH - 1)) {
				stopY = iH - 1;
				stop = nHalfDots + (iH - 1 - Y);
			} else {
				stop = nHalfDots * 2;
			}

			if (start > 0 || stop < nDots - 1) {
				CalTempContrib(start, stop, contrib, tmpContrib);
				for (int x = 0; x < iW; x++) {
					value = VerticalFilter(pbImage, startY, stopY, start, stop,
							x, tmpContrib);
					pbOut.setRGB(x, y, value);
				}
			} else {
				for (int x = 0; x < iW; x++) {
					value = VerticalFilter(pbImage, startY, stopY, start, stop,
							x, normContrib);
					pbOut.setRGB(x, y, value);
				}
			}

		}

		return pbOut;
	}

	private static int VerticalFilter(BufferedImage pbInImage, int startY,
			int stopY, int start, int stop, int x, double[] pContrib) {
		double valueRed = 0.0;
		double valueGreen = 0.0;
		double valueBlue = 0.0;
		int valueRGB = 0;
		int i, j;

		for (i = startY, j = start; i <= stopY; i++, j++) {
			valueRGB = pbInImage.getRGB(x, i);
			valueRed += GetRedValue(valueRGB) * pContrib[j];
			valueGreen += GetGreenValue(valueRGB) * pContrib[j];
			valueBlue += GetBlueValue(valueRGB) * pContrib[j];
		}

		valueRGB = ComRGB(Clip((int) valueRed), Clip((int) valueGreen),
				Clip((int) valueBlue));
		return valueRGB;

	}

	private static int ComRGB(int redValue, int greenValue, int blueValue) {

		return (redValue << 16) + (greenValue << 8) + blueValue;
	}

	private static int GetRedValue(int rgbValue) {
		int temp = rgbValue & 0x00ff0000;
		return temp >> 16;
	}

	private static int GetGreenValue(int rgbValue) {
		int temp = rgbValue & 0x0000ff00;
		return temp >> 8;
	}

	private static int GetBlueValue(int rgbValue) {
		return rgbValue & 0x000000ff;
	}

	private static int Clip(int x) {
		if (x < 0)
			return 0;
		if (x > 255)
			return 255;
		return x;
	}
	
	/**
	 * 创建缩约图
	 * @param filename 源文件
	 * @param thumbWidth 缩约图宽
	 * @param thumbHeight 缩约图高
	 * @param quality 质量
	 * @param outFilename 缩约图宽名称
	 * @throws InterruptedException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void createThumbnail(String filename, int thumbWidth,
			int thumbHeight/*, int quality*/, String outFilename)
			throws InterruptedException, FileNotFoundException, IOException {
		// load image from filename
		Image image = Toolkit.getDefaultToolkit().getImage(filename);
		MediaTracker mediaTracker = new MediaTracker(new Container());
		mediaTracker.addImage(image, 0);
		mediaTracker.waitForID(0);
		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int[] size = size(image);
		double imageRatio = (double) size[0] / (double) size[1];
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}
		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

		// save thumbnail image to outFilename
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(outFilename));
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
//		quality = Math.max(0, Math.min(quality, 100));
//		param.setQuality((float) quality / 100.0f, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(thumbImage);
		out.close();
	}
	/** 
     * 强制压缩/放大图片到固定的大小 
     * @param inputImage 原图片
     * @param w int 新宽度 
     * @param h int 新高度 
     * @param targetFile 新图片
     */  
    public static void resizeToFixSize(InputStream inputImage,int w, int h,String targetFile) {
    	FileOutputStream out = null;
    	try {
			if (inputImage == null)
				throw new RuntimeException("图片不存在！");
			BufferedImage read = ImageIO.read(inputImage);
			// SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的 优先级比速度高 生成的图片质量比较好 但速度慢  
			BufferedImage image = new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB );   
			image.getGraphics().drawImage(read, 0, 0, w, h, null); // 绘制缩小后的图  
			out = new FileOutputStream(targetFile); // 输出到文件流  
			// 可以正常实现bmp、png、gif转jpg  
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);  
			encoder.encode(image); // JPEG编码  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			FileUtil.release(inputImage, out);
		}
    } 
    /** 
     * 强制压缩/放大图片到固定的大小 
     * @param sourceFile 原图片
     * @param w int 新宽度 
     * @param h int 新高度 
     * @param targetFile 新图片
     */  
    public static void resizeToFixSize(String sourceFile,int w, int h,String targetFile){  
    	if (StringUtile.isEmptyString(sourceFile))
			throw new RuntimeException("图片不存在！");
    	FileInputStream inputImage = null;
		try {
			inputImage = new FileInputStream(sourceFile);
			resizeToFixSize(inputImage, w, h, targetFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			FileUtil.release(inputImage, null);
		}
    }
    
    /** 
     * 强制压缩/放大图片到固定的大小 
     * @param sourceFile 原图片
     * @param w int 新宽度 
     * @param h int 新高度 
     * @param targetFile 新图片
     */  
    public static void resizeToFixSize(File sourceFile,int w, int h,String targetFile){  
    	if (sourceFile == null || !sourceFile.exists())
			throw new RuntimeException("图片不存在！");
    	FileInputStream inputImage = null;
		try {
			inputImage = new FileInputStream(sourceFile);
			resizeToFixSize(inputImage, w, h, targetFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			FileUtil.release(inputImage, null);
		}
    }
}