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
/* global Ractive, HUBU */

//Initialize Ractive.templates
(function(){
  "use strict";
  if(Ractive.templates === undefined){ Ractive.templates = {}; }
})();

/**
 * Implementation of the {@link RactiveRenderService} based on [Ractive]{@link http://ractivejs.org}
 *
 * @class RactiveRender
 * @extends HUBU.AbstractComponent
 * @extends RactiveRenderService
 */
function RactiveRender(){
  "use strict";

  var self = this;
  var _hub;
  var _ractive;

  var _model; //ractive data
  var _template; //ractive template
  var _container; //ractive el

  var _autorender = false; //render on start
  var _lazy = false; //lazy two way binding
  var _twoway = true; //two way binding

  self.name = "RactiveRenderDefault";

  self.getComponentName = function(){
    return self.name;
  };

  /**
   * Configure the instance of the RactiveRender component.
   *
   * @method configure
   * @param {HUBU.hub} theHub
   * @param  conf - The RactiveRender configuration.
   * @param {map} conf.model - The model link to the view to be render.
   * @param {string|function} conf.container - The dom element in wich the rendered view must be injected.
   * @param {string} conf.template - The Ractive template use in order to render the view.
   * @param {boolean} [conf.autorender=false] - If true the view will be render when this component start.
   * @param {boolean} [conf.lazy=false] - Lazy two way binding.
   * @param {boolean} [conf.twoway=true] - Activate/Desactivate two way binding between the view and the model.
   */
  self.configure = function(theHub,conf){
    _hub = theHub;

    if(typeof conf.model === undefined){
      throw "The `model` configuration property is mandatory.";
    }

    if(typeof conf.template === undefined){
      throw "The `template` configuration property is mandatory.";
    }

    if(typeof conf.container === undefined){
      throw "The `container` configuration property is mandatory.";
    }

    _model = conf.model;

    if(typeof conf.template === "string" && Ractive.templates[conf.template] !== undefined){
      _template = Ractive.templates[conf.template];
    } else{
      _template = conf.template;
    }

    _container = conf.container;

    if(typeof conf.name === "string"){
      self.name = conf.name;
    }

    if(typeof conf.autorender === "boolean"){
      _autorender = conf.autorender;
    }

    if(typeof conf.twoway === "boolean"){
      _twoway = conf.twoway;
    }

    if(typeof conf.lazy === "boolean"){
      _lazy = conf.lazy;
    }

    //register the RactiveRenderService
    _hub.provideService({
      component : self,
      contract : window.RactiveRenderService,
      properties : {
        autorender : _autorender,
        twoway : _twoway,
        lazy : _lazy
      }
    });

    //register the RenderService
    _hub.provideService({
      component : self,
      contract : window.RenderService,
      properties : {
        autorender : _autorender,
        twoway : _twoway,
        lazy : _lazy
      }
    });
  };

  self.start = function(){
    if(_autorender){
      self.render();
    }
  };

  self.stop = function(){
    self.teardown();
    _ractive=undefined;
  };

  self.render = function(success){
    self.teardown(); // if already running

    _ractive = new Ractive({
      el: _container,
      template: _template,
      data: _model,
      oncomplete: success,
      lazy: _lazy,
      twoway: _twoway
    });
  };

  self.set = function(key, value, success){
    if(_ractive === undefined){
      HUBU.logger.debug("This ractive has not been rendered, you need to render it by calling render().");
      _model[key] = value;
      return;
    }

    _ractive.set(key, value, success);
  };

  self.update = function(key, success){
    if(_ractive === undefined){
      HUBU.logger.debug("This ractive has not been rendered, you need to render it by calling render().");
      return;
    }

    _ractive.update(key, success);
  };

  self.on = function(eventName, handler){
    if(_ractive === undefined){
      HUBU.logger.debug("This ractive has not been rendered.");
      return;
    }

    return _ractive.on(eventName, handler);
  };

  self.observe = function(key, observer, options){
    if(_ractive === undefined){
      HUBU.logger.debug("This ractive has not been rendered.");
      return;
    }

    return _ractive.observe(key, observer, options);
  };

  self.find = function(selector){
    if(_ractive === undefined){
        HUBU.logger.debug("This ractive has not been rendered.");
      return;
    }

    return _ractive.find(selector);
  };

  self.teardown = function(){
    if(_ractive!==undefined){
      _ractive.teardown();
    }
  };
}
