package org.starfire.shine.svg.inkscape;

import org.starfire.shine.geom.Ellipse;
import org.starfire.shine.geom.Shape;
import org.starfire.shine.geom.Transform;
import org.starfire.shine.svg.Diagram;
import org.starfire.shine.svg.Figure;
import org.starfire.shine.svg.Loader;
import org.starfire.shine.svg.NonGeometricData;
import org.starfire.shine.svg.ParsingException;
import org.w3c.dom.Element;

/**
 * Processor for <ellipse> and <path> nodes marked as arcs
 *
 * @author kevin
 */
public class EllipseProcessor implements ElementProcessor {
	
	/**
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#process(org.starfire.shine.svg.Loader, org.w3c.dom.Element, org.starfire.shine.svg.Diagram, org.starfire.shine.geom.Transform)
	 */
	public void process(Loader loader, Element element, Diagram diagram, Transform t) throws ParsingException {
		Transform transform = Util.getTransform(element);
		transform = new Transform(t, transform);
		
		float x = Util.getFloatAttribute(element,"cx");
		float y = Util.getFloatAttribute(element,"cy");
		float rx = Util.getFloatAttribute(element,"rx");
		float ry = Util.getFloatAttribute(element,"ry");
		
		Ellipse ellipse = new Ellipse(x,y,rx,ry);
		Shape shape = ellipse.transform(transform);

		NonGeometricData data = Util.getNonGeometricData(element);
		data.addAttribute("cx", ""+x);
		data.addAttribute("cy", ""+y);
		data.addAttribute("rx", ""+rx);
		data.addAttribute("ry", ""+ry);
		
		diagram.addFigure(new Figure(Figure.ELLIPSE, shape, data, transform));
	}

	/**
	 * @see org.starfire.shine.svg.inkscape.ElementProcessor#handles(org.w3c.dom.Element)
	 */
	public boolean handles(Element element) {
		if (element.getNodeName().equals("ellipse")) {
			return true;
		}
		if (element.getNodeName().equals("path")) {
			if ("arc".equals(element.getAttributeNS(Util.SODIPODI, "type"))) {
				return true;
			}
		}
		
		return false;
	}

}
