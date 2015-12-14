package org.starfire.shine.svg.inkscape;

import org.starfire.shine.geom.Transform;
import org.starfire.shine.svg.Diagram;
import org.starfire.shine.svg.Loader;
import org.starfire.shine.svg.ParsingException;
import org.w3c.dom.Element;

/**
 * TODO: Document this class
 *
 * @author kevin
 */
public class GroupProcessor implements ElementProcessor {

	/**
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#handles(org.w3c.dom.Element)
	 */
	public boolean handles(Element element) {
		if (element.getNodeName().equals("g")) {
			return true;
		}
		return false;
	}

	/**O
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#process(org.starfire.shine.svg.Loader, org.w3c.dom.Element, org.starfire.shine.svg.Diagram, org.starfire.shine.geom.Transform)
	 */
	public void process(Loader loader, Element element, Diagram diagram, Transform t) throws ParsingException {
		Transform transform = Util.getTransform(element);
		transform = new Transform(t, transform);
		
		loader.loadChildren(element, transform);
	}

}
