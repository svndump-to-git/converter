/*
 * Copyright 2013 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.svn.tools.merge;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.kuali.student.svn.tools.merge.model.BranchData;
import org.kuali.student.svn.tools.merge.tools.BranchUtils;

/**
 * @author Kuali Student Team
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class TestBranchUtils {

	/**
	 * 
	 */
	public TestBranchUtils() {
	}

	
	@Test
	public void testBranchUtils() {
		
		BranchData bd = BranchUtils.parse(123456L, "poc/personidentity/personidentity-api/branches/personidentity-api-dev/src/main/java/org/kuali/student/poc/xsd/personidentity/person/dto/AttributeSetDefinition.java");
	
		Assert.assertNotNull(bd);
		
		Assert.assertEquals("poc/personidentity/personidentity-api/branches/personidentity-api-dev", bd.getBranchPath());
		Assert.assertEquals("src/main/java/org/kuali/student/poc/xsd/personidentity/person/dto/AttributeSetDefinition.java", bd.getPath());
		Assert.assertEquals(Long.valueOf(123456L), bd.getRevision());
		
		
		
		bd = BranchUtils.parse(123456L, "deploymentlab/trunk/1.0.x/ks-cfg-dbs/ks-embedded-db/src/main/impex/KREN_CHNL_PRODCR_T.xml");
		
		Assert.assertNotNull(bd);
		
		Assert.assertEquals("deploymentlab/trunk", bd.getBranchPath());
		Assert.assertEquals("1.0.x/ks-cfg-dbs/ks-embedded-db/src/main/impex/KREN_CHNL_PRODCR_T.xml", bd.getPath());
		Assert.assertEquals(Long.valueOf(123456L), bd.getRevision());
		

	
	bd = BranchUtils.parse(2237L, "enumeration/enumeration-impl/src/main/java/org/kuali/student/enumeration/entity/ContextDAO.java");
		
		Assert.assertNotNull(bd);
		
		Assert.assertEquals("enumeration/enumeration-impl", bd.getBranchPath());
		Assert.assertEquals("src/main/java/org/kuali/student/enumeration/entity/ContextDAO.java", bd.getPath());
		Assert.assertEquals(Long.valueOf(2237L), bd.getRevision());
	}
	
	@Test 
	public void testMissingDataCase() {
		// 2249:2250::sandbox/team2/branches:branches:lum

		BranchData bd = BranchUtils.parse(2249L, "branches");
		
		Assert.assertNotNull(bd);
		
		Assert.assertEquals("branches", bd.getBranchPath());
		Assert.assertEquals ("", bd.getPath());
		
		bd = BranchUtils.parse(2250L, "sandbox/team2/lum/branches");
		
		Assert.assertNotNull(bd);
		
		Assert.assertEquals("sandbox/team2/lum/branches", bd.getBranchPath());
		Assert.assertEquals ("", bd.getPath());
		
	}
}
