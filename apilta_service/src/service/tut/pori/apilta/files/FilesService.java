/**
 * Copyright 2016 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package service.tut.pori.apilta.files;

import java.util.Arrays;

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.StringParameter;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * service definitions for files service
 * 
 */
@HTTPService(name = Definitions.SERVICE_FILES)
public class FilesService {
	/**
	 * 
	 * @param temporaryToken 
	 * @return see {@link service.tut.pori.apilta.files.datatypes.FileDetailsList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GET_FILE_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getFileDetails (
			@HTTPMethodParameter(name = Definitions.PARAMETER_TEMPORARY_TOKEN) StringParameter temporaryToken
			) 
	{
		FileDetails details = FilesCore.getFileDetailsForToken(temporaryToken.getValue());
		if(details == null){
			return new Response(Status.FORBIDDEN);
		}else{
			FileDetailsList list = new FileDetailsList();
			list.setFiles(Arrays.asList(details));
			return new Response(list);
		}
	}
}
