package com.github.qianniancc.compress.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipException;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;


/**
 * 压缩或解压zip：
 * 由于直接使用java.util.zip工具包下的类，会出现中文乱码问题，所以使用ant.jar中的org.apache.tools.zip下的工具类
 * @author Administrator
 */

public class ZipUtil {
    private static byte[] _byte = new byte[1024];
    
    /**
     * 压缩文件或路径
     * @param zip 压缩的目的地址
     * @param srcFiles 压缩的源文件
     */
    public static void zipFile( String zip , List<File> srcFiles ){
    	System.out.println(zip);
        try {
            if( zip.endsWith(".zip") || zip.endsWith(".ZIP") ){
                ZipOutputStream _zipOut = new ZipOutputStream(new FileOutputStream(new File(zip))) ;
                _zipOut.setEncoding("GBK");
                for( File _f : srcFiles ){
                    handlerFile(zip , _zipOut , _f , "");
                }
                _zipOut.close();
            }else{
                System.out.println("target file[" + zip + "] is not .zip type file");
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }
    
    /**
     * 
     * @param zip 压缩的目的地址
     * @param zipOut 
     * @param srcFile  被压缩的文件信息
     * @param path  在zip中的相对路径
     * @throws IOException
     */
    private static void handlerFile(String zip , ZipOutputStream zipOut , File srcFile , String path ) throws IOException{
        System.out.println(" begin to compression file[" + srcFile.getName() + "]");
        if( !"".equals(path) && ! path.endsWith(File.separator)){
            path += File.separator ;
        }
        if( ! srcFile.getPath().equals(zip) ){
            if( srcFile.isDirectory() ){
                File[] _files = srcFile.listFiles() ;
                if( _files.length == 0 ){
                    zipOut.putNextEntry(new ZipEntry( path + srcFile.getName() + File.separator));
                    zipOut.closeEntry();
                }else{
                    for( File _f : _files ){
                        handlerFile( zip ,zipOut , _f , path + srcFile.getName() );
                    }
                }
            }else{
                InputStream _in = new FileInputStream(srcFile) ;
                zipOut.putNextEntry(new ZipEntry(path + srcFile.getName()));
                int len = 0 ; 
                while( (len = _in.read(_byte)) > 0  ){
                    zipOut.write(_byte, 0, len);
                }
                _in.close();
                zipOut.closeEntry();
            }
        }
    }

    /**
     * 解压缩ZIP文件，将ZIP文件里的内容解压到targetDIR目录下
     * @param zipName 待解压缩的ZIP文件名
     * @param targetBaseDirName  目标目录
     */
    public static List<File> unzipFile(String zipPath, String descDir) {
        return unzipFile( new File(zipPath) , descDir ) ;
    }
    
    /**
     * 对.zip文件进行解压缩
     * @param zipFile  解压缩文件
     * @param descDir  压缩的目标地址，如：D:\\测试 或 /mnt/d/测试
     * @return
     */
    public static List<File> unzipFile(File zipFile, String descDir) {
        List<File> _list = new ArrayList<File>() ;
        try {
            ZipFile _zipFile = new ZipFile(zipFile , "GBK") ;
            for( Enumeration<?> entries = _zipFile.getEntries() ; entries.hasMoreElements() ; ){
                ZipEntry entry = (ZipEntry)entries.nextElement() ;
                File _file = new File(descDir + File.separator + entry.getName()) ;
                if( entry.isDirectory() ){
                    _file.mkdirs() ;
                }else{
                    File _parent = _file.getParentFile() ;
                    if( !_parent.exists() ){
                        _parent.mkdirs() ;
                    }
                    InputStream _in = _zipFile.getInputStream(entry);
                    OutputStream _out = new FileOutputStream(_file) ;
                    int len = 0 ;
                    while( (len = _in.read(_byte)) > 0){
                        _out.write(_byte, 0, len);
                    }
                    _in.close(); 
                    _out.flush();
                    _out.close();
                    _list.add(_file) ;
                } 
            }
            _zipFile.close();
        } catch (IOException e) {
        }
        return _list ;
    }
    
    
    /**
     * 打开.zip文件
     * @param zipFile 要打开的文件
     * @return
     */
    public static List<String> openzipFile(File zipFile) {
        List<String> _list = new ArrayList<String>() ;
        ZipFile _zipFile = null;
		try {
			_zipFile = new ZipFile(zipFile , "GBK");
			for( Enumeration<?> entries = _zipFile.getEntries() ; entries.hasMoreElements() ; ){
	            ZipEntry entry = (ZipEntry)entries.nextElement();
	            _list.add(entry.getName());
	        }
			_zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return _list ;
    }
    
    /**
     * 对临时生成的文件夹和文件夹下的文件进行删除
     */
    public static void deletefile(String delpath) {
        try {
            File file = new File(delpath);
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File delfile = new File(delpath + File.separator + filelist[i]);
                    if (!delfile.isDirectory()) {
                        delfile.delete();
                    } else if (delfile.isDirectory()) {
                        deletefile(delpath + File.separator + filelist[i]);
                    }
                }
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** 
     * 读取zip文件中制定文件的内容 
     * @param zipFile 目标zip文件对象 
     * @param readFileName 目标读取文件名字 
     * @return 文件内容 
     * @throws ZipException 
     * @throws IOException 
     */  
    public static String getZipFileContent(File zipFile, String readFileName) throws IOException {  
        StringBuilder content = new StringBuilder();  
        ZipFile zip = new ZipFile(zipFile);  
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.getEntries();  
          
        ZipEntry ze;  
        // 枚举zip文件内的文件/  
        while (entries.hasMoreElements()) {  
            ze = entries.nextElement();  
            // 读取目标对象  
            if (ze.getName().equals(readFileName)) {  
                Scanner scanner = new Scanner(zip.getInputStream(ze));  
                while (scanner.hasNextLine()) {  
                    content.append(scanner.nextLine());  
                }  
                scanner.close();  
            }  
        }  
        zip.close();  
          
        return content.toString();  
    }  
    
}