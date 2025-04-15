# Jervis Resources

This module is a temporary module while we figure out exactly how to store and retrieve information
about players, rosters and icons across the various projects. Right now it contains the definition 
for BB2020 Rosters as they appear in the rulebook.

It is mostly tests that wants the information, but these already have their own copies in `jervis-test-utils`,
so the only reason this module hasn't been merged back into `jervis-ui` is because we are currently using 
the roster information in `fumbbl-net`. This is the wrong approach, and we should build the rosters from
the FUMBBL API rather than hoping the Jervis default ones match.
