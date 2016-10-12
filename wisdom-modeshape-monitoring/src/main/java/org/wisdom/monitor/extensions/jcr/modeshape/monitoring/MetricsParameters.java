/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2016 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.monitor.extensions.jcr.modeshape.monitoring;

import org.modeshape.jcr.api.monitor.DurationMetric;
import org.modeshape.jcr.api.monitor.ValueMetric;
import org.modeshape.jcr.api.monitor.Window;

/**
 * Created by KEVIN on 11/10/2016.
 */
public class MetricsParameters {
    ValueMetric valueMetric;
    DurationMetric durationMetric;
    Window window;

    public ValueMetric getValueMetric() {
        return valueMetric;
    }

    public void setValueMetric(ValueMetric valueMetric) {
        this.valueMetric = valueMetric;
    }

    public DurationMetric getDurationMetric() {
        return durationMetric;
    }

    public void setDurationMetric(DurationMetric durationMetric) {
        this.durationMetric = durationMetric;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(Window window) {
        this.window = window;
    }
}
