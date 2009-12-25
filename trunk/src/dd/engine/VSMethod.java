package dd.engine;
/** Vertex skipping method, for skipping some vertices without
    affecting the overall shape of the frontier. It controls how eps
    is used in the {@link dd.engine.Frontier#buildFrontier(Test[],
    FrontierContext, int, Vector) frontier-building} method
*/
public enum VSMethod { 
    /** The simplest (and recommended) method: ignore (skip) a vertex
     * if it's within eps from an already "recorded" vertex both in
     * the cost and detection-rate terms.
     */
    VM1, 
	/** The idea of this method is to limit the area of each
	excluded triangle to no more than eps*c/2, where c is the
	length of the preserved segment of the frontier. This means
	that the total area of excluded triangles, along the entire
	frontierm, will be no more than eps.
	 */
	VM2,
	/** A sophisticated vertex-skipping method proposed by Endre
	Boros in the document "DNDO.C.D.curves.MenuPart2.doc"
	(2009-05-12).

	It is implemented as follows:
	
	<pre>
  For a given input s:

  initialize M := 0;  
  initialize i := s+1;

  while(  i &le; N   AND   (D(i)-D(s))/(C(i)-C(s))  &ge;  M/(1+eps) ) {
     Set  M := max{ M,  ((D(i)-(1+eps)*D(s)) / (C(i)-C(s)) };
     Set  i := i+1;    
  }

  return i(s) := i-1;
  </pre>
	 */
	EB1 };
