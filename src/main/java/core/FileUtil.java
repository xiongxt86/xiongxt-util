package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/** 
 * @Title: FileUtil.java 
 * @Description: TODO 
 * @author ChenYa 
 * @date 2014-7-9 下午9:54:51 
 * @version V1.0 
 */
public class FileUtil {

	public static boolean isDirExist(String path, boolean isCreate){
		File file = new File(path);
		boolean isExist = file.exists() && file.isDirectory();
		if(!isExist && isCreate)
			file.mkdir();
		return isExist;
	} 
	
	
	public static void copyFile(File sourcefile,File targetFile) throws IOException{
        
        //新建文件输入流并对它进行缓冲
        FileInputStream input=new FileInputStream(sourcefile);
        BufferedInputStream inbuff=new BufferedInputStream(input);
        
        //新建文件输出流并对它进行缓冲
        FileOutputStream out=new FileOutputStream(targetFile);
        BufferedOutputStream outbuff=new BufferedOutputStream(out);
        
        //缓冲数组
        byte[] b=new byte[1024*5];
        int len=0;
        while((len=inbuff.read(b))!=-1){
            outbuff.write(b, 0, len);
        }
        
        //刷新此缓冲的输出流
        outbuff.flush();
        
        //关闭流
        inbuff.close();
        outbuff.close();
        out.close();
        input.close();
        
	}
	
    public static void copyDirectiory(String sourceDir,String targetDir) throws IOException {
        
        //新建目标目录
        
        (new File(targetDir)).mkdirs();
        
        //获取源文件夹当下的文件或目录
        File[] file=(new File(sourceDir)).listFiles();
        
        for (int i = 0; i < file.length; i++) {
            if(file[i].isFile()){
                //源文件
                File sourceFile=file[i];
                    //目标文件
                File targetFile=new File(new File(targetDir).getAbsolutePath()+File.separator+file[i].getName());
                
                copyFile(sourceFile, targetFile);
            
            }
            
            if(file[i].isDirectory()){
                //准备复制的源文件夹
                String dir1=sourceDir+file[i].getName();
                //准备复制的目标文件夹
                String dir2=targetDir+"/"+file[i].getName();
                
                copyDirectiory(dir1, dir2);
            }
        }
        
    }
	
    
   //删除文件夹
   //param folderPath 文件夹完整绝对路径 
   public static void delFolder(String folderPath)  throws IOException  {
	   
       delAllFile(folderPath); //删除完里面所有内容
       
       String filePath = folderPath;
       
       filePath = filePath.toString();
       
       java.io.File myFilePath = new java.io.File(filePath);
       
       if( myFilePath.exists() ){

    	   myFilePath.delete(); //删除空文件夹
       }
   
    }
    
    
    //删除指定文件夹下所有文件
    //param path 文件夹完整绝对路径
   
    public static boolean delAllFile(String path)  throws IOException {
    	
        boolean flag = false;
        
        File file = new File(path);
        
        if (!file.exists()) {
          return flag;
        }
        
        if (!file.isDirectory()) {
          return flag;
        }
        
        String[] tempList = file.list();
        
        File temp = null;
        
        for (int i = 0; i < tempList.length; i++) {
          if (path.endsWith(File.separator)) {
              temp = new File(path + tempList[i]);
           } else {
               temp = new File(path + File.separator + tempList[i]);
           }
           if (temp.isFile()) {
              temp.delete();
           }
           if (temp.isDirectory()) {
              delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
              delFolder(path + "/" + tempList[i]);//再删除空文件夹
              flag = true;
           }
        }
        return flag;
   }     
    
   
   /**
    * 文件名    
    * @param fileName
    * @return
    */
   public static String getFileName( String fileName ){
		int index = fileName.lastIndexOf(".");
		
		if (index != -1) {
			return fileName.substring(0, index);
		}
		
		return "";
   }
   
   
   /**
    * 文件后缀
    * @param fileName
    * @return
    */
   public static String getExtName( String fileName ){
	   
	   int index = fileName.lastIndexOf(".");
		if (index != -1) {
			return fileName.substring(0, index);
		}
		return "";
   }
   public static void copyFiles(String path1, String path2) throws Exception {
       // TODO Auto-generated method stub

       File file = new File(path1);
       if(file.isDirectory())
       {
           File f = new File(path2);
           if(!f.exists()) f.mkdir();
           File[] files = file.listFiles();
           for (File file2 : files) {
               //System.out.println(file2.toString()+"-----"+path2+"/"+file2.getName());

               copyFiles(file2.toString(),path2+"/"+file2.getName());
           }
           
       }else
       {
           copy(path1,path2);
       }
   }
   
   //拷贝单个文件的方法

   public static void copy(String path1,String path2) throws IOException {
       // TODO Auto-generated method stub

       
               
       DataInputStream in = new DataInputStream(
               new BufferedInputStream(
                       new FileInputStream(path1)));
       
       byte[] date = new byte[in.available()];
       
       in.read(date);
       
       DataOutputStream out = new DataOutputStream(
               new BufferedOutputStream(
                       new FileOutputStream(path2)));
       out.write(date);
       
       in.close();
       out.close();

   }
   /**
	 * 判断是否为图片
	 * @param file
	 * @return
	 */
	public static boolean isImage(String name){
		if(name == null || "".equals(name)) 
			return false;
		String[] images_ = new String[]{".jpg",".png",".gif",".ioc",".bmp",".jpeg"};
		List<String> images = Arrays.asList(images_);
		String subfix = suffix(name);
		String prefix = prefix(name);
		return subfix != null && prefix != null && images.contains(subfix);//有前缀，有后缀，且格式正确
	}
	
	/**
	 * 获取文件后缀 
	 * @param filename 文件名
	 * @return
	 */
	public static String suffix(String filename){
		if(filename == null || "".equals(filename))
			return null;
		int subfixindex = filename.lastIndexOf(".");
		if(subfixindex < 0) 
			return null;
		String subfix = filename.substring(subfixindex);
		return subfix;
	}
	
	/**
	 * 获取文件前缀 
	 * @param filename 文件名
	 * @return
	 */
	public static String prefix(String filename){
		if(filename == null || "".equals(filename))
			return null;
		int prefixindex = filename.lastIndexOf(".");
		if(prefixindex < 0) 
			return null;
		String prefix = filename.substring(0,prefixindex);
		return prefix;
	}
	/**
	 * 给路径添加反斜杠
	 * @param path
	 * @return
	 */
	public static String addBackslash(String path){
		String result = "";
		result = path.replace("\\\\", "/");
		result = result.replace("\\", "/");
		if(!result.endsWith("/"))
			result = result + "/";
		return result;
	}
	
	/**
	 * 内存数据拷贝到文件
	 * 
	 * @param input 内存输入流
	 * @param file 保存文件
	 * @return
	 */
	public static void copyInputStreamToFile(InputStream input, File file) {
		if(input == null) return;
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			byte[] data = new byte[1024];
			int read = 0;
			while (read != -1) {
				read = input.read(data, 0, 1000);
				os.write(data, 0, read);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			release(input, os);
		}
	}
	
	/**
	 * 回收资源
	 * 
	 * @param input
	 *            内存输入流
	 * @param path
	 *            保存路径
	 * @return
	 */
	public static void release(InputStream input, OutputStream output) {
		try {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 为文件名加_logo
	 * @param fileName
	 * @return
	 */
	public static String addLogo(String fileName){
		String result = "";
		if(StringUtile.isEmptyString(fileName)) return "";
		String suffix = FileUtil.suffix(fileName);
		String prefix = FileUtil.prefix(fileName);
		result = prefix + "_logo" + suffix;
		return result;
	}
}
