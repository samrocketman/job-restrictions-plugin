/*
 * The MIT License
 *
 * Copyright 2013 Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.synopsys.arc.jenkinsci.plugins.jobrestrictions.jobs;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.Messages;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Oleg Nenashev <nenashev@synopsys.com>, Synopsys Inc.
 */
public class JobRestrictionProperty extends JobProperty {
    
    UpstreamCauseRestriction upstreamCauseRestriction;

    @DataBoundConstructor
    public JobRestrictionProperty(UpstreamCauseRestriction upstreamCauseRestriction) {
        this.upstreamCauseRestriction = upstreamCauseRestriction;
    }

    public UpstreamCauseRestriction getUpstreamCauseRestriction() {
        return upstreamCauseRestriction;
    }
  
    @Override
    public boolean prebuild(AbstractBuild build, BuildListener listener) {
        // Consider build as valid if any cause is valid
        for ( Object cause : build.getCauses() ) {
           try {
            validateCause((Cause)cause, listener);
           } catch (AbortException ex) {
               //TODO: Throw Aborted upstairs in the future
               listener.fatalError("[Job Restrictions] - Build will be aborted: "+ex.getMessage());
               return false;
           }
        }

        // Build is valid
        return true;
    }
    
    private void validateCause(Cause cause, BuildListener listener) throws AbortException { 
       if (upstreamCauseRestriction != null && cause instanceof Cause.UpstreamCause) {
           upstreamCauseRestriction.validate((Cause.UpstreamCause)cause);
       }    
       //TODO: checks
       
    }
       
    @Extension
    public static class DescriptorImpl extends JobPropertyDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.jobs_JobRestrictionProperty_DisplayName();
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }
        
    }
}
