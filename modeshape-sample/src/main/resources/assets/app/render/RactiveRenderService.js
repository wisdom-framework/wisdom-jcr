/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
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
/**
 * The RactiveRenderService provides the two way binding capabilities of the [Ractive]{@link http://ractivejs.org} framework
 * in addition to the @{link RenderService} functionalities.
 *
 * @class RactiveRenderService
 * @extends RenderService
 * @global
 * @abstract
 */
window.RactiveRenderService = (function(renderService){
  "use strict";

  var rr = Object.create(null);

  //extends RenderService
  for (var i in renderService) {
     rr[i] = renderService[i];
  }

  /**
   * Teardown the ractive view rendered through this service, once teardown,
   * the view is still present but all obersers, and two way binding are off.
   *
   * @method teardown
   * @memberof RRenderService
   */
  rr.teardown=function(){};

  /**
   * Subscribe to ractive [events]{@link https://github.com/Rich-Harris/Ractive/wiki/Events}.
   *
   * @method on
   * @memberof RRenderService
   * @param {string} eventName - The name of the event on which to subscribe.
   * @param {function} handler - The function called on event (with ractive as this).
   * @return {object} - Calling [cancel] method on the returned object will cancel the handler.
   */
  rr.on=function(eventName, handler){};

  /**
   * Remove an event handler, serveral event handlers or all!
   *
   * @method off
   * @memberof RRenderService
   * @param {string} eventName - The name of the event on which to unsubscribe.
   * @param {function} handler - The event handler to remove.
   */
   //rr.off=function(eventName, handler){};

  /**
   * Observe the model entry of given key. The [observer] will be initialised when created with
   *  [undefined] as [oldValue].
   *
   * @method observe
   * @memberof RRenderService
   * @param {string} key - The model entry key to observe
   * @param {function} observer - The function that will be called with [newValue] and [oldvalue]
   *                               as argument whenever the observed model entry change value.
   * @param {object} [options] -
   *                 {boolean} [init=true] -
   *                 {boolean} [defer=false] -
   *                 {object}  [context=ractive] -
   * @return {object} - Calling [cancel] method on the returned object will cancel the observer.
   */
  rr.observe=function(key, observer, options){};

  return rr;
})(window.RenderService);