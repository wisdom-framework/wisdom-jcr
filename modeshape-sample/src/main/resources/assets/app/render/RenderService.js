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
 * Specification of the RenderService, which allows to render a view from a js template and an associated model.
 * Futhermore this service allows for the synchronisation of the model with the view and two way bindings if
 * supported by the implementation.
 *
 * @class RenderService
 * @global
 * @abstract
 */
window.RenderService = (function(){
  "use strict";

  var rs = Object.create(null);

  /**
   * Render the view linked to this service with the model given during configuration.
   * Render the Ractive view linked to this service.
   *
   * @method render
   * @memberof RenderService
   */
  rs.render=function(){};

  /**
   * Notify that the model has change without passing by this service
   * and that the updated entry need to be rerender.
   *
   * @method update
   * @memberof RenderService
   * @param {string} [key] - The key of the model entry that has been change.
   * @param {function} [success] - callback call when all change has been completed.
   */
  rs.update=function(key,success){};

  /**
   * Set/update a data entry of the model, and triggered change in the view.
   * (rerender the view, or only the entry if supported)
   *
   * @method set
   * @memberof RenderService
   * @param {string} key - the key of the data we want to set/update.
   * @param {string} value - the value of the data we want to set/update.
   * @param {function} [success] - callback call when all change has been completed.
   * Render the Ractive view linked to this service.
   */
  rs.set=function(key, value, success){};

  /**
   * Find an en element of the renderered view.
   *
   * @method find
   * @memberof RenderService
   * @param {string} selector - The CSS selector of the element to find.
   * @return {Node} the elements inside this Ractive instance matching the selecor.
   */
  rs.find=function(selector){};

  return rs;
})();