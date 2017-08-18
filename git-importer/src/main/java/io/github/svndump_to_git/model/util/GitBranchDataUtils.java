/**
 * 
 */
package io.github.svndump_to_git.model.util;

import io.github.svndump_to_git.model.GitBranchData;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import io.github.svndump_to_git.git.model.BranchMergeInfo;
import io.github.svndump_to_git.git.model.ExternalModuleUtils;
import io.github.svndump_to_git.git.model.SvnRevisionMapper;
import io.github.svndump_to_git.git.model.tree.GitTreeData;
import io.github.svndump_to_git.git.model.tree.utils.GitTreeProcessor;
import io.github.svndump_to_git.svn.model.ExternalModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ocleirig
 * 
 */
public final class GitBranchDataUtils {

	private static Logger log = LoggerFactory
			.getLogger(GitBranchDataUtils.class);

	/**
	 * 
	 */
	private GitBranchDataUtils() {
	}

	public static void extractExternalModules(Repository repo,
			GitTreeData root, GitBranchData targetBranchData,
			final GitTreeProcessor treeProcessor)
			throws MissingObjectException, IOException {

		final List<ExternalModuleInfo> existingExternals = new ArrayList<>(5);

		// load up any existing svn:externals data
		ObjectId blobId = root.find(repo, "fusion-maven-plugin.dat");

		if (blobId != null) {
			List<String> existingData = treeProcessor
					.getBlobAsStringLines(blobId);

			existingExternals.addAll(ExternalModuleUtils
					.extractFusionMavenPluginData(existingData));
		}

		targetBranchData.setExternals(existingExternals);
	}

	public static void extractAndStoreBranchMerges(long sourceRevision,
			String sourceBranchName, GitBranchData targetBranchData,
			SvnRevisionMapper revisionMapper) throws IOException {
		// load up any existing svn:mergeinfo data
		List<BranchMergeInfo> existingMergeInfo = revisionMapper
				.getLatestMergeBranches(sourceBranchName);

		if (existingMergeInfo != null && existingMergeInfo.size() > 0)
			targetBranchData.accumulateMergeInfo(existingMergeInfo);

	}

}
