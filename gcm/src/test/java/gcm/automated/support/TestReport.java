package gcm.test.support;

import java.util.LinkedHashSet;
import java.util.Set;

import gcm.output.reports.AbstractReport;
import gcm.output.reports.StateChange;

public class TestReport extends AbstractReport{

	@Override
	public Set<StateChange> getListenedStateChanges() {
		return new LinkedHashSet<>();
	}
}
