/*
 * Copyright 2014 The Kuali Foundation
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
package org.kuali.student.git.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.kuali.student.git.model.GitTreeProcessor.GitTreeBlobVisitor;
import org.kuali.student.git.model.branch.BranchDetector;
import org.kuali.student.git.model.exceptions.VetoBranchException;
import org.kuali.student.git.model.util.GitTreeDataUtils;
import org.kuali.student.git.utils.GitBranchUtils;
import org.kuali.student.git.utils.GitBranchUtils.ILargeBranchNameProvider;
import org.kuali.student.svn.tools.merge.model.BranchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Represents the
 * 
 * @author Kuali Student Team
 * 
 */
public class GitBranchData {

	public static final Logger log = LoggerFactory
			.getLogger(GitBranchData.class);

	private GitTreeData branchRoot = new GitTreeData();

	private String branchPath;

	private String branchName;

	private ObjectId parentId;

	private Set<ObjectId> mergeParentIdSet = new HashSet<ObjectId>();

	private AtomicLong blobsAdded = new AtomicLong(0L);

	private boolean created;

	private ILargeBranchNameProvider largeBranchNameProvider;

	private long revision;

	private BranchDetector branchDetector;

	private GitTreeProcessor treeProcessor;

	private boolean alreadyInitialized = false;

	/**
	 * @param revision
	 * @param branchPath
	 * @param revision 
	 * @param largeBranchNameProvider 
	 * @param path
	 * 
	 */
	public GitBranchData(String branchName, long revision, ILargeBranchNameProvider largeBranchNameProvider, GitTreeProcessor treeProcessor, BranchDetector branchDetector) {
		this.treeProcessor = treeProcessor;
		this.branchDetector = branchDetector;
		this.branchPath = GitBranchUtils.getBranchPath(branchName, revision, largeBranchNameProvider);
		this.revision = revision;
		this.largeBranchNameProvider = largeBranchNameProvider;
		this.branchName = GitBranchUtils.getCanonicalBranchName(this.branchPath, revision, largeBranchNameProvider);

	}

	/**
	 * @return the branchPath
	 */
	public String getBranchPath() {
		return branchPath;
	}

	/**
	 * @return the branchName
	 */
	public String getBranchName() {
		return branchName;
	}

	public long getBlobsAdded() {
		return blobsAdded.get();
	}

	public void addBlob(String path, String blobSha1, PrintWriter blobLog)
			throws VetoBranchException, MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {

		if (!path.startsWith(this.branchPath)) {
			String errorMessage = String.format("blob absolute path(%s) does not match this branch (%s)", path, this.branchName);
			log.error(errorMessage);
			blobLog.println(errorMessage);
			return;
		}

		initialize();
		
		blobsAdded.addAndGet(1L);

		BranchData db = branchDetector.parseBranch(revision, path);

		String filePath = db.getPath();

		if (filePath.length() == 0) {
			String errorMessage = String.format ("trying to index an empty file path.  Path = %s, File Path = %s, blobId = %s, ", path, filePath, blobSha1);
			
			log.warn(errorMessage);
			blobLog.println(errorMessage);
			
			/*
			 * Indexing an empty file breaks the JGit Tree so exclude the file.
			 */
			return;
		}

		branchRoot.addBlob(filePath, blobSha1);

	}

	/**
	 * @param inserter
	 * @return
	 * @throws IOException
	 * @see org.kuali.student.git.model.GitTreeData#buildTree(org.eclipse.jgit.lib.ObjectInserter)
	 */
	public ObjectId buildTree(ObjectInserter inserter) throws IOException {
		return branchRoot.buildTree(inserter);

	}

	/**
	 * Apply our from the dump tree data onto the existing data
	 * 
	 * @param existingTreeData
	 */
	public void mergeOntoExistingTreeData(GitTreeData existingTreeData) {
		branchRoot.mergeOntoExisting(existingTreeData);
	}

	public int getBlobCount() {
		return GitTreeDataUtils.countBlobs(branchRoot);
	}

	public void deletePath(String path, long currentRevision) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		
		initialize();
		
		// should we strip of the branch name part of the path and only pass
		// through the
		// remaining path.

		if (path.startsWith(branchPath)) {

			StringBuilder withinBranchPath = new StringBuilder(path);

			withinBranchPath.delete(0, branchPath.length());

			if (withinBranchPath.charAt(0) == '/')
				withinBranchPath.deleteCharAt(0);

			branchRoot.deletePath(withinBranchPath.toString());
		} else {
			log.warn("invalid branch");
		}

	}

	
	
	private void initialize() throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {
		
		if (alreadyInitialized || parentId == null)
			return;
		
		alreadyInitialized = true;
		
		GitTreeData existingTreeData = GitTreeDataUtils.extractExistingTreeData(treeProcessor, parentId);

		int existingBlobCount = GitTreeDataUtils
				.countBlobs(existingTreeData);

		mergeOntoExistingTreeData(existingTreeData);

		int mergedBlobCount = getBlobCount();

		if (existingBlobCount != mergedBlobCount) {
			throw new RuntimeException(
					"data loss existing count = "
							+ existingBlobCount
							+ ", merged count = " + mergedBlobCount);
		}
		
	}

	public Set<ObjectId> getMergeParentIds() {

		return Collections.unmodifiableSet(this.mergeParentIdSet);

	}

	public void setParentId(ObjectId parentId) {
		this.parentId = parentId;

	}

	/**
	 * @return the parentId
	 */
	public ObjectId getParentId() {
		return parentId;
	}

	public void addMergeParentId(ObjectId head) {
		this.mergeParentIdSet.add(head);
	}

	public void setCreated(boolean created) {
		this.created = created;

	}

	/**
	 * @return the created
	 */
	public boolean isCreated() {
		return created;
	}

	public void reset() {

		created = false;
		blobsAdded.set(0L);
		branchRoot = new GitTreeData();
		mergeParentIdSet.clear();
		parentId = null;

	}

}