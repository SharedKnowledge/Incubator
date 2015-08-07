package utils;

import interfaces.Comment;

import java.util.Comparator;

/**
 * This class offers comparison of Comments by their creation date.
 * 
 *
 */
public class CommentCreationDateComparator implements Comparator<Comment> {

	/**
	 * This method compares two Comments by their creation date.
	 * 
	 * @param c1 the first Comment that is compared to the second
	 * @param c2 the second Comment that is compared to the first
	 * @return 0 if the Comments (their creation dates) are considered equal, 
	 * 				 <0 if c1 is considered smaller (meaning: earlier), >0 if larger 
	 * 				 than c2 
	 */
	@Override
	public int compare(Comment c1, Comment c2) {
		return c1.getCreationDate().compareTo(c2.getCreationDate());
	}
}
