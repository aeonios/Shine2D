package org.starfire.shine.svg.inkscape;

import org.starfire.shine.geom.Rectangle;
import org.starfire.shine.geom.Shape;
import org.starfire.shine.geom.Transform;
import org.starfire.shine.svg.Diagram;
import org.starfire.shine.svg.Figure;
import org.starfire.shine.svg.Loader;
import org.starfire.shine.svg.NonGeometricData;
import org.starfire.shine.svg.ParsingException;
import org.w3c.dom.Element;

/**
 * A processor for the <rect> element.
 *
 * @author kevin
 */
public class RectProcessor implements ElementProcessor {

	/**
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#process(org.starfire.shine.svg.Loader, org.w3c.dom.Element, org.starfire.shine.svg.Diagram, org.starfire.shine.geom.Transform)
	 */
	public void process(Loader loader, Element element, Diagram diagram, Transform t) throws ParsingException {
		Transform transform = Util.getTransform(element);
	    transform = new Transform(t, transform); 
		
		float width = Float.parseFloat(element.getAttribute("width"));
		float height = Float.parseFloat(element.getAttribute("height"));
		float x = Float.parseFloat(element.getAttribute("x"));
		float y = Float.parseFloat(element.getAttribute("y"));
		
		Rectangle rect = new Rectangle(x,y,width+1,height+1);
		Shape shape = rect.transform(transform);
		
		NonGeometricData data = Util.getNonGeometricData(element);
		data.addAttribute("width", ""+width);
		data.addAttribute("height", ""+height);
		data.addAttribute("x", ""+x);
		data.addAttribute("y", ""+y);
		
		diagram.addFigure(new Figure(Figure.RECTANGLE, shape, data, transform));
	}

	/**
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#handles(org.w3c.dom.Element)
	 */
	public boolean handles(Element element) {
		if (element.getNodeName().equals("rect")) {
			return true;
		}
		
		return false;
	}
}
