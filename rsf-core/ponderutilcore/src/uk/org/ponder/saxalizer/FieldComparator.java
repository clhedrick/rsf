/*
 * Created on Oct 15, 2004
 */
package uk.org.ponder.saxalizer;

import java.util.Comparator;

import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Returns a comparator capable of comparing any objects with {valid
 * SAXalizer mappings for a get accessor method with a given name} by
 * a member mapped by the accessor.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *  
 */
public class FieldComparator implements Comparator {

  public static SAXAccessMethod findSingleGetter(Class objclass,
      SAXalizerMappingContext context, String tagname) {
    MethodAnalyser ma = MethodAnalyser.getMethodAnalyser(objclass, context);
    SAXAccessMethod method = ma.tagmethods.get(tagname);
    if (method == null) {
      method = ma.attrmethods.get(tagname);
    }
    if (!method.canGet() || !method.ismultiple) {
      throw new UniversalRuntimeException(
          "Located access method of unsuitable type for name " + tagname
              + " in " + objclass);
    }
    return method;
  }
  
  private SAXAccessMethod accessor;
  public FieldComparator(Class objclass, SAXalizerMappingContext context,
      String fieldname) {
    accessor = findSingleGetter(objclass, context, fieldname);
  }

  public int compare(Object o1, Object o2) {
    Comparable field1 = (Comparable) accessor.getChildObject(o1);
    Comparable field2 = (Comparable) accessor.getChildObject(o2);
    return field1.compareTo(field2);
  }

}