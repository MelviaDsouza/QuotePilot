package com.quotepilot.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AwardReadiness {

    private boolean readyForApproval;
    private int totalChecks;
    private int passedChecks;
    private int unresolvedIssues;

    private List<String> issues;
    private List<String> passedChecksList;
}