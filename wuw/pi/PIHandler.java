package wuw.pi;


/**
 * This interface defines the services provided by WUW's Program Interface
 * module.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 30
 */
public interface PIHandler {


// items - remote peers - pending downloads/uploads - finished downloads/uploads
/**
 * Retrieve information about a content's download/upload status. This is the
 * way WUW gets to know the details about a content (see
 * {@link wuw.core.ContentStatus}).
 * 
 * @param ContentID
 *          The unique ID of this content, as known from the message sent to the
 *          topology provider.
 * @return The latest updates about the content trading on the local peer.
 */
Transaction[] getContentUpdates();

}
