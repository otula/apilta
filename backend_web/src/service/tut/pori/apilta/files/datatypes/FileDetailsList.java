package service.tut.pori.apilta.files.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * list of file details.
 */
@XmlRootElement(name=Definitions.ELEMENT_FILE_DETAILS_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class FileDetailsList extends ResponseData {
	@XmlElement(name=Definitions.ELEMENT_FILE_DETAILS)
	private List<FileDetails> _files = null;

	/**
	 * @return the files
	 * @see #setFiles(List)
	 */
	public List<FileDetails> getFiles() {
		return _files;
	}

	/**
	 * @param files the files to set
	 * @see #getFiles()
	 */
	public void setFiles(List<FileDetails> files) {
		_files = files;
	}
	
	/**
	 * 
	 * @param details
	 * @see #getFiles()
	 */
	public void addFile(FileDetails details) {
		if(_files == null){
			_files = new ArrayList<>();
		}
		_files.add(details);
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if empty
	 * @see #isEmpty(FileDetailsList)
	 */
	protected boolean isEmpty() {
		return (_files == null || _files.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if list is null or empty
	 */
	public static boolean isEmpty(FileDetailsList list) {
		return (list == null || list.isEmpty());
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(FileDetailsList)
	 */
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}else{
			for(FileDetails details : _files){
				if(!FileDetails.isValid(details)){
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is not null and valid
	 */
	public static boolean isValid(FileDetailsList list) {
		return (list != null && list.isValid());
	}
}
