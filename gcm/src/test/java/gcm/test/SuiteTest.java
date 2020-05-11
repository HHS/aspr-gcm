package gcm.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import gcm.test.automated.AT_AbstractComponent;
import gcm.test.automated.AT_ActionType;
import gcm.test.automated.AT_ArrayIntSet;
import gcm.test.automated.AT_BooleanContainer;
import gcm.test.automated.AT_DimensionTree;
import gcm.test.automated.AT_DoubleValueContainer;
import gcm.test.automated.AT_Earth;
import gcm.test.automated.AT_EnumContainer;
import gcm.test.automated.AT_EnvironmentImpl_01;
import gcm.test.automated.AT_EnvironmentImpl_02;
import gcm.test.automated.AT_EnvironmentImpl_03;
import gcm.test.automated.AT_EnvironmentImpl_04;
import gcm.test.automated.AT_EnvironmentImpl_05;
import gcm.test.automated.AT_EnvironmentImpl_06;
import gcm.test.automated.AT_EnvironmentImpl_07;
import gcm.test.automated.AT_EnvironmentImpl_08;
import gcm.test.automated.AT_EnvironmentImpl_09;
import gcm.test.automated.AT_EnvironmentImpl_10;
import gcm.test.automated.AT_EnvironmentImpl_11;
import gcm.test.automated.AT_EnvironmentImpl_12;
import gcm.test.automated.AT_EnvironmentImpl_13;
import gcm.test.automated.AT_EnvironmentImpl_14;
import gcm.test.automated.AT_EnvironmentImpl_15;
import gcm.test.automated.AT_EnvironmentImpl_16;
import gcm.test.automated.AT_EnvironmentImpl_17;
import gcm.test.automated.AT_EnvironmentImpl_18;
import gcm.test.automated.AT_EnvironmentImpl_19;
import gcm.test.automated.AT_EnvironmentImpl_20;
import gcm.test.automated.AT_EnvironmentImpl_21;
import gcm.test.automated.AT_EnvironmentImpl_22;
import gcm.test.automated.AT_EnvironmentImpl_23;
import gcm.test.automated.AT_Equality;
import gcm.test.automated.AT_ExperimentBuilder;
import gcm.test.automated.AT_Filters;
import gcm.test.automated.AT_FloatValueContainer;
import gcm.test.automated.AT_GraphPathSolver;
import gcm.test.automated.AT_HashIntSet;
import gcm.test.automated.AT_IntId;
import gcm.test.automated.AT_IntValueContainer;
import gcm.test.automated.AT_LatLon;
import gcm.test.automated.AT_LatLonAlt;
import gcm.test.automated.AT_LatLonBox;
import gcm.test.automated.AT_MapOption;
import gcm.test.automated.AT_MemoryLink;
import gcm.test.automated.AT_MemoryPartition;
import gcm.test.automated.AT_MemoryReportItem;
import gcm.test.automated.AT_MultiKey;
import gcm.test.automated.AT_MutableStat;
import gcm.test.automated.AT_ObjectValueContainer;
import gcm.test.automated.AT_PlanningQueueReportItem;
import gcm.test.automated.AT_PropertyDefinition;
import gcm.test.automated.AT_ReplicationImpl;
import gcm.test.automated.AT_ReportPeriod;
import gcm.test.automated.AT_Simulation;
import gcm.test.automated.AT_SimulationErrorType;
import gcm.test.automated.AT_StateChange;
import gcm.test.automated.AT_StochasticPersonSelection;
import gcm.test.automated.AT_StructuredScenarioBuilder;
import gcm.test.automated.AT_TimeTrackingPolicy;
import gcm.test.automated.AT_UnstructuredScenarioBuilder;
import gcm.test.automated.AT_MutableVector2D;
import gcm.test.automated.AT_MutableVector3D;

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
	AT_Filters.class,
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
	AT_MutableVector2D.class
	})

public class SuiteTest {

}
