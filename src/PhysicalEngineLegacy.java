/**
 * Copyright 2008 code_swarm project team
 *
 * This file is part of code_swarm.
 *
 * code_swarm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * code_swarm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with code_swarm.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * @brief Legacy algorithms describing all physicals interactions between nodes (files and persons)
 * 
 * This is only a rewriting of the initial code_swarm prototype.
 * 
 * @see other Physical Engine for more methods 
 */
public class PhysicalEngineLegacy extends PhysicalEngine
{
  final private float FORCE_CALCULATION_RANDOMIZER;
  final private float FORCE_TO_SPEED_MULTIPLIER;
  final private float SPEED_TO_POSITION_MULTIPLIER;
  
  /**
   * Constructor for initializing parameters.
   */
  PhysicalEngineLegacy(float forceCalculationRandomizer, float forceToSpeedMultiplier, float speedToPositionMultiplier)
  {
    FORCE_CALCULATION_RANDOMIZER  = forceCalculationRandomizer;
    FORCE_TO_SPEED_MULTIPLIER     = forceToSpeedMultiplier;
    SPEED_TO_POSITION_MULTIPLIER  = speedToPositionMultiplier;
  }
  
  /**
   * Legacy method that calculate the attractive/repulsive force between a person and one of its file along their link (the edge).
   * 
   * @param[in] edge the link between a person and one of its file 
   * @param[out] force calculated between those two nodes
   */
  public void calculateForceAlongAnEdge( code_swarm.Edge edge, Vector force )
  {
    float distance;
    float fakeForce;
    
    // distance calculation
    force.set( edge.getNodeTo().getX() - edge.getNodeFrom().getX(), edge.getNodeTo().getY() - edge.getNodeFrom().getY() );
    distance = force.getNorm();
    if (distance > 0) {
      // fake force calculation (increase when distance is different from targeted len")
      fakeForce = (edge.getLen() - distance) / (distance * 3);
      // force ponderation using a re-mapping life from 0-255 scale to 0-1.0 range
      fakeForce = fakeForce * (edge.life * 1.0f) / 255;
      // fake force projection onto x and y axis
      force.multiply( fakeForce );
    }
  }
  
  /**
   * Legacy method that calculate the repulsive force between two similar nodes (either files or persons).
   * 
   * @param[in] nodeA
   * @param[in] nodeB
   * @param[out] force calculated between those two nodes
   */
  public void calculateForceBetweenNodes( code_swarm.Node nodeA, code_swarm.Node nodeB, Vector force )
  {
    float distx, disty;
    float lensq;
    
    /** TODO: add comment to this algorithm */
    distx = nodeA.getX() - nodeB.getX();
    disty = nodeA.getY() - nodeB.getY();
    lensq = distx * distx + disty * disty;
    if (lensq == 0) {
      force.set( (float)Math.random()*FORCE_CALCULATION_RANDOMIZER, (float)Math.random()*FORCE_CALCULATION_RANDOMIZER );
    } else if (lensq < 10000) {
      force.set( distx / lensq, disty / lensq );
    }
  }
  
  /**
   * Legacy method that apply a force to a node, converting acceleration to speed.
   * 
   * @param[in]  Node the node to which the force apply
   * @param[in]  force a force Vector representing the force on a node
   * 
   * TODO: does force should be a property of the node (or not?)
   */
  public void applyForceTo( code_swarm.Node node, Vector force )
  {
    float dlen;

    /** TODO: add comment to this algorithm */
    dlen = force.getNorm() / 2;
    if (dlen > 0) {
      node.addDX( force.getX() / dlen );
      node.addDY( force.getY() / dlen );
    }
    node.mulDX( FORCE_TO_SPEED_MULTIPLIER );
    node.mulDY( FORCE_TO_SPEED_MULTIPLIER );
  }

  /**
   * Legacy method that apply a force to a node, converting acceleration to speed.
   * 
   * @param node the node to which the force apply
    */
  public void applySpeedTo( code_swarm.Node node )
  {
    float div;
    // This block enforces a maximum absolute velocity.
    if (node.getSpeed() > node.maxSpeed) {
      Vector mag = new Vector(node.getDX() / node.maxSpeed, node.getDY() / node.maxSpeed);
      div = mag.getNorm();
      node.mulDX( 1/div );
      node.mulDY( 1/div );
    }
    
    // This block convert Speed to Position
    node.addX( node.getDX() );
    node.addY( node.getDY() );
    
    // Apply drag (reduce Speed for next frame calculation)
    node.mulDX( SPEED_TO_POSITION_MULTIPLIER );
    node.mulDY( SPEED_TO_POSITION_MULTIPLIER );
  }
}
