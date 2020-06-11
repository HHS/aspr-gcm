package gcm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import gcm.automated.AT_AbstractComponent;
import gcm.automated.AT_ActionType;
import gcm.automated.AT_ArrayIntSet;
import gcm.automated.AT_ArrayPathSolver;
import gcm.automated.AT_BinContainer;
import gcm.automated.AT_BooleanContainer;
import gcm.automated.AT_DimensionTree;
import gcm.automated.AT_DoubleValueContainer;
import gcm.automated.AT_Earth;
import gcm.automated.AT_EnumContainer;
import gcm.automated.AT_EnvironmentImpl_01;
import gcm.automated.AT_EnvironmentImpl_02;
import gcm.automated.AT_EnvironmentImpl_03;
import gcm.automated.AT_EnvironmentImpl_04;
import gcm.automated.AT_EnvironmentImpl_05;
import gcm.automated.AT_EnvironmentImpl_06;
import gcm.automated.AT_EnvironmentImpl_07;
import gcm.automated.AT_EnvironmentImpl_08;
import gcm.automated.AT_EnvironmentImpl_09;
import gcm.automated.AT_EnvironmentImpl_10;
import gcm.automated.AT_EnvironmentImpl_11;
import gcm.automated.AT_EnvironmentImpl_12;
import gcm.automated.AT_EnvironmentImpl_13;
import gcm.automated.AT_EnvironmentImpl_14;
import gcm.automated.AT_EnvironmentImpl_15;
import gcm.automated.AT_EnvironmentImpl_16;
import gcm.automated.AT_EnvironmentImpl_17;
import gcm.automated.AT_EnvironmentImpl_18;
import gcm.automated.AT_EnvironmentImpl_19;
import gcm.automated.AT_EnvironmentImpl_20;
import gcm.automated.AT_EnvironmentImpl_21;
import gcm.automated.AT_EnvironmentImpl_22;
import gcm.automated.AT_EnvironmentImpl_23;
import gcm.automated.AT_Equality;
import gcm.automated.AT_ExperimentBuilder;
import gcm.automated.AT_Filter;
import gcm.automated.AT_FloatValueContainer;
import gcm.automated.AT_GeoLocator;
import gcm.automated.AT_Graph;
import gcm.automated.AT_GraphDepthEvaluator;
import gcm.automated.AT_GraphPathSolver;
import gcm.automated.AT_Graphs;
import gcm.automated.AT_HashIntSet;
import gcm.automated.AT_ImmutableStat;
import gcm.automated.AT_IntId;
import gcm.automated.AT_IntValueContainer;
import gcm.automated.AT_LatLon;
import gcm.automated.AT_LatLonAlt;
import gcm.automated.AT_LatLonBox;
import gcm.automated.AT_MapOption;
import gcm.automated.AT_MapPathSolver;
import gcm.automated.AT_MemoryLink;
import gcm.automated.AT_MemoryPartition;
import gcm.automated.AT_MemoryReportItem;
import gcm.automated.AT_MultiKey;
import gcm.automated.AT_MutableGraph;
import gcm.automated.AT_MutableStat;
import gcm.automated.AT_MutableVector2D;
import gcm.automated.AT_MutableVector3D;
import gcm.automated.AT_ObjectValueContainer;
import gcm.automated.AT_Path;
import gcm.automated.AT_Paths;
import gcm.automated.AT_PlanningQueueReportItem;
import gcm.automated.AT_PropertyDefinition;
import gcm.automated.AT_ReplicationImpl;
import gcm.automated.AT_ReportPeriod;
import gcm.automated.AT_Simulation;
import gcm.automated.AT_SimulationErrorType;
import gcm.automated.AT_SphericalArc;
import gcm.automated.AT_SphericalPoint;
import gcm.automated.AT_SphericalPolygon;
import gcm.automated.AT_SphericalTriangle;
import gcm.automated.AT_StateChange;
import gcm.automated.AT_StochasticPersonSelection;
import gcm.automated.AT_StructuredScenarioBuilder;
import gcm.automated.AT_TimeTrackingPolicy;
import gcm.automated.AT_UnstructuredScenarioBuilder;
import gcm.automated.AT_Vector2D;
import gcm.automated.AT_Vector3D;
import gcm.automated.AT_VolumetricDimensionTree;

/**
 * This class executes automated JUnit tests for GCM. Automated tests are noted
 * with "AT_" as a prefix as opposed to "MT_" for manual tests. Automated tests
 * are designed to be small and rapid running tests allowing them to be run
 * during a standard build. This suite class allows for central management of
 * these tests.
 *
 * @author Shawn Hatch
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
	AT_ReplicationImpl.class,
	AT_Simulation.class,
	AT_EnvironmentImpl_01.class,
	AT_EnvironmentImpl_02.class,
	AT_EnvironmentImpl_03.class,
	AT_EnvironmentImpl_04.class,
	AT_EnvironmentImpl_05.class,
	AT_EnvironmentImpl_06.class,
	AT_EnvironmentImpl_07.class,
	AT_EnvironmentImpl_08.class,
	AT_EnvironmentImpl_09.class,
	AT_EnvironmentImpl_10.class,
	AT_EnvironmentImpl_11.class,
	AT_EnvironmentImpl_12.class,
	AT_EnvironmentImpl_13.class,
	AT_EnvironmentImpl_14.class,
	AT_EnvironmentImpl_15.class,
	AT_EnvironmentImpl_16.class,
	AT_EnvironmentImpl_17.class,
	AT_EnvironmentImpl_18.class,
	AT_EnvironmentImpl_19.class,
	AT_EnvironmentImpl_20.class,
	AT_EnvironmentImpl_21.class,
	AT_EnvironmentImpl_22.class,
	AT_EnvironmentImpl_23.class,
	AT_MultiKey.class,
	AT_Filter.class,
	AT_IntValueContainer.class,
	AT_DoubleValueContainer.class,
	AT_BooleanContainer.class,
	AT_FloatValueContainer.class,
	AT_ArrayIntSet.class,
	AT_HashIntSet.class,
	AT_ObjectValueContainer.class,
	AT_EnumContainer.class,
	AT_StructuredScenarioBuilder.class,
	AT_UnstructuredScenarioBuilder.class,	
	AT_TimeTrackingPolicy.class,
	AT_ReportPeriod.class,
	AT_StateChange.class,
	AT_MapOption.class,
	AT_SimulationErrorType.class,
	AT_ActionType.class,
	AT_Equality.class,
	AT_AbstractComponent.class,
	AT_PropertyDefinition.class,
	AT_ExperimentBuilder.class,
	AT_BooleanContainer.class,
	AT_MemoryLink.class,
	AT_IntId.class,
	AT_StochasticPersonSelection.class,
	AT_MutableStat.class,
	AT_MemoryPartition.class,
	AT_PlanningQueueReportItem.class,
	AT_MemoryReportItem.class,
	AT_DimensionTree.class,
	AT_GraphPathSolver.class,
	AT_LatLon.class,
	AT_LatLonAlt.class,
	AT_LatLonBox.class,
	AT_Earth.class,
	AT_MutableVector3D.class,
	AT_MutableVector2D.class,
	AT_Vector2D.class,
	AT_Vector3D.class,
	AT_SphericalPoint.class,
	AT_SphericalArc.class,
	AT_SphericalTriangle.class,
	AT_SphericalPolygon.class,
	AT_GeoLocator.class,
	AT_Graph.class,
	AT_MutableGraph.class,
	AT_Graphs.class,
	AT_GraphDepthEvaluator.class,
	AT_Path.class,
	AT_Paths.class,
	AT_VolumetricDimensionTree.class,
	AT_MapPathSolver.class,
	AT_ArrayPathSolver.class,
	AT_BinContainer.class,
	AT_ImmutableStat.class
	})

public class SuiteTest {

}
