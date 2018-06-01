'use strict';

function _interopDefault (ex) { return (ex && (typeof ex === 'object') && 'default' in ex) ? ex['default'] : ex; }

var three = require('three');
var ThreeDragControls = _interopDefault(require('three-dragcontrols'));
var ThreeForceGraph = _interopDefault(require('three-forcegraph'));
var ThreeRenderObjects = _interopDefault(require('three-render-objects'));
var accessorFn = _interopDefault(require('accessor-fn'));
var Kapsule = _interopDefault(require('kapsule'));

function styleInject(css, ref) {
  if (ref === void 0) ref = {};
  var insertAt = ref.insertAt;

  if (!css || typeof document === 'undefined') {
    return;
  }

  var head = document.head || document.getElementsByTagName('head')[0];
  var style = document.createElement('style');
  style.type = 'text/css';

  if (insertAt === 'top') {
    if (head.firstChild) {
      head.insertBefore(style, head.firstChild);
    } else {
      head.appendChild(style);
    }
  } else {
    head.appendChild(style);
  }

  if (style.styleSheet) {
    style.styleSheet.cssText = css;
  } else {
    style.appendChild(document.createTextNode(css));
  }
}

var css = ".graph-nav-info {\n  bottom: 5px;\n  width: 100%;\n  text-align: center;\n  color: slategrey;\n  opacity: 0.7;\n  font-size: 10px;\n}\n\n.graph-info-msg {\n  top: 50%;\n  width: 100%;\n  text-align: center;\n  color: lavender;\n  opacity: 0.7;\n  font-size: 22px;\n}\n\n.graph-tooltip {\n  color: lavender;\n  font-size: 18px;\n  transform: translate(-50%, 25px);\n}\n\n.graph-info-msg, .graph-nav-info, .graph-tooltip {\n  position: absolute;\n  font-family: Sans-serif;\n}\n\n.grabbable {\n  cursor: move;\n  cursor: grab;\n  cursor: -moz-grab;\n  cursor: -webkit-grab;\n}\n\n.grabbable:active {\n  cursor: grabbing;\n  cursor: -moz-grabbing;\n  cursor: -webkit-grabbing;\n}";
styleInject(css);

function linkKapsule (kapsulePropName, kapsuleType) {

  var dummyK = new kapsuleType(); // To extract defaults

  return {
    linkProp: function linkProp(prop) {
      // link property config
      return {
        default: dummyK[prop](),
        onChange: function onChange(v, state) {
          state[kapsulePropName][prop](v);
        },

        triggerUpdate: false
      };
    },
    linkMethod: function linkMethod(method) {
      // link method pass-through
      return function (state) {
        var kapsuleInstance = state[kapsulePropName];

        for (var _len = arguments.length, args = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
          args[_key - 1] = arguments[_key];
        }

        var returnVal = kapsuleInstance[method].apply(kapsuleInstance, args);

        return returnVal === kapsuleInstance ? this // chain based on the parent object, not the inner kapsule
        : returnVal;
      };
    }
  };
}

var defineProperty = function (obj, key, value) {
  if (key in obj) {
    Object.defineProperty(obj, key, {
      value: value,
      enumerable: true,
      configurable: true,
      writable: true
    });
  } else {
    obj[key] = value;
  }

  return obj;
};

var _extends = Object.assign || function (target) {
  for (var i = 1; i < arguments.length; i++) {
    var source = arguments[i];

    for (var key in source) {
      if (Object.prototype.hasOwnProperty.call(source, key)) {
        target[key] = source[key];
      }
    }
  }

  return target;
};

var toConsumableArray = function (arr) {
  if (Array.isArray(arr)) {
    for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i];

    return arr2;
  } else {
    return Array.from(arr);
  }
};

var three$1 = window.THREE ? window.THREE // Prefer consumption from global THREE, if exists
: { AmbientLight: three.AmbientLight, DirectionalLight: three.DirectionalLight };

//

var CAMERA_DISTANCE2NODES_FACTOR = 150;

//

// Expose config from forceGraph
var bindFG = linkKapsule('forceGraph', ThreeForceGraph);
var linkedFGProps = Object.assign.apply(Object, toConsumableArray(['jsonUrl', 'graphData', 'numDimensions', 'nodeRelSize', 'nodeId', 'nodeVal', 'nodeResolution', 'nodeColor', 'nodeAutoColorBy', 'nodeOpacity', 'nodeThreeObject', 'linkSource', 'linkTarget', 'linkColor', 'linkAutoColorBy', 'linkOpacity', 'linkWidth', 'linkResolution', 'linkCurvature', 'linkCurveRotation', 'linkMaterial', 'linkDirectionalParticles', 'linkDirectionalParticleSpeed', 'linkDirectionalParticleWidth', 'linkDirectionalParticleColor', 'linkDirectionalParticleResolution', 'forceEngine', 'd3AlphaDecay', 'd3VelocityDecay', 'warmupTicks', 'cooldownTicks', 'cooldownTime'].map(function (p) {
  return defineProperty({}, p, bindFG.linkProp(p));
})));
var linkedFGMethods = Object.assign.apply(Object, toConsumableArray(['d3Force'].map(function (p) {
  return defineProperty({}, p, bindFG.linkMethod(p));
})));

// Expose config from renderObjs
var bindRenderObjs = linkKapsule('renderObjs', ThreeRenderObjects);
var linkedRenderObjsProps = Object.assign.apply(Object, toConsumableArray(['width', 'height', 'backgroundColor', 'showNavInfo', 'enablePointerInteraction'].map(function (p) {
  return defineProperty({}, p, bindRenderObjs.linkProp(p));
})));
var linkedRenderObjsMethods = Object.assign.apply(Object, toConsumableArray(['cameraPosition'].map(function (p) {
  return defineProperty({}, p, bindRenderObjs.linkMethod(p));
})));

//

var _3dForceGraph = Kapsule({

  props: _extends({
    nodeLabel: { default: 'name', triggerUpdate: false },
    linkLabel: { default: 'name', triggerUpdate: false },
    linkHoverPrecision: { default: 1, onChange: function onChange(p, state) {
        return state.renderObjs.lineHoverPrecision(p);
      }, triggerUpdate: false },
    enableNodeDrag: { default: true, triggerUpdate: false },
    onNodeClick: { default: function _default() {}, triggerUpdate: false },
    onNodeHover: { default: function _default() {}, triggerUpdate: false },
    onLinkClick: { default: function _default() {}, triggerUpdate: false },
    onLinkHover: { default: function _default() {}, triggerUpdate: false }
  }, linkedFGProps, linkedRenderObjsProps),

  aliases: { // Prop names supported for backwards compatibility
    nameField: 'nodeLabel',
    idField: 'nodeId',
    valField: 'nodeVal',
    colorField: 'nodeColor',
    autoColorBy: 'nodeAutoColorBy',
    linkSourceField: 'linkSource',
    linkTargetField: 'linkTarget',
    linkColorField: 'linkColor',
    lineOpacity: 'linkOpacity'
  },

  methods: _extends({
    stopAnimation: function stopAnimation(state) {
      if (state.animationFrameRequestId) {
        cancelAnimationFrame(state.animationFrameRequestId);
      }
      return this;
    },
    scene: function scene(state) {
      return state.renderObjs.scene();
    } }, linkedFGMethods, linkedRenderObjsMethods),

  stateInit: function stateInit() {
    return {
      forceGraph: new ThreeForceGraph(),
      renderObjs: ThreeRenderObjects()
    };
  },

  init: function init(domNode, state) {
    // Wipe DOM
    domNode.innerHTML = '';

    // Add relative container
    domNode.appendChild(state.container = document.createElement('div'));
    state.container.style.position = 'relative';

    // Add renderObjs
    var roDomNode = document.createElement('div');
    state.container.appendChild(roDomNode);
    state.renderObjs(roDomNode);
    var camera = state.renderObjs.camera();
    var renderer = state.renderObjs.renderer();
    var tbControls = state.renderObjs.tbControls();
    state.lastSetCameraZ = camera.position.z;

    // Add info space
    var infoElem = void 0;
    state.container.appendChild(infoElem = document.createElement('div'));
    infoElem.className = 'graph-info-msg';
    infoElem.textContent = '';

    // config forcegraph
    state.forceGraph.onLoading(function () {
      infoElem.textContent = 'Loading...';
    });
    state.forceGraph.onFinishLoading(function () {
      infoElem.textContent = '';

      // sync graph data structures
      state.graphData = state.forceGraph.graphData();

      // re-aim camera, if still in default position (not user modified)
      if (camera.position.x === 0 && camera.position.y === 0 && camera.position.z === state.lastSetCameraZ) {
        camera.lookAt(state.forceGraph.position);
        state.lastSetCameraZ = camera.position.z = Math.cbrt(state.graphData.nodes.length) * CAMERA_DISTANCE2NODES_FACTOR;
      }

      // Setup node drag interaction
      if (state.enableNodeDrag && state.enablePointerInteraction && state.forceEngine === 'd3') {
        // Can't access node positions programatically in ngraph
        var dragControls = new ThreeDragControls(state.graphData.nodes.map(function (node) {
          return node.__threeObj;
        }), camera, renderer.domElement);

        dragControls.addEventListener('dragstart', function (event) {
          tbControls.enabled = false; // Disable trackball controls while dragging

          var node = event.object.__data;
          node.__initialFixedPos = { fx: node.fx, fy: node.fy, fz: node.fz };

          // lock node
          ['x', 'y', 'z'].forEach(function (c) {
            return node['f' + c] = node[c];
          });

          // keep engine running at low intensity throughout drag
          state.forceGraph.d3AlphaTarget(0.3);

          // drag cursor
          renderer.domElement.classList.add('grabbable');
        });

        dragControls.addEventListener('drag', function (event) {
          state.ignoreOneClick = true; // Don't click the node if it's being dragged

          var node = event.object.__data;

          // Move fx/fy/fz (and x/y/z) of nodes based on object new position
          ['x', 'y', 'z'].forEach(function (c) {
            return node['f' + c] = node[c] = event.object.position[c];
          });

          // prevent freeze while dragging
          state.forceGraph.resetCountdown();
        });

        dragControls.addEventListener('dragend', function (event) {
          var node = event.object.__data;
          var initPos = node.__initialFixedPos;

          if (initPos) {
            ['x', 'y', 'z'].forEach(function (c) {
              var fc = 'f' + c;
              if (initPos[fc] === undefined) {
                node[fc] = undefined;
              }
            });
            delete node.__initialFixedPos;
          }

          state.forceGraph.d3AlphaTarget(0) // release engine low intensity
          .resetCountdown(); // let the engine readjust after releasing fixed nodes

          tbControls.enabled = true; // Re-enable trackball controls

          // clear cursor
          renderer.domElement.classList.remove('grabbable');
        });
      }
    });

    // config renderObjs
    var getGraphObj = function getGraphObj(object) {
      var obj = object;
      // recurse up object chain until finding the graph object (only if using custom nodes)
      while (state.nodeThreeObject && obj && !obj.hasOwnProperty('__graphObjType')) {
        obj = obj.parent;
      }
      return obj;
    };

    state.renderObjs.objects([// Populate scene
    new three$1.AmbientLight(0xbbbbbb), new three$1.DirectionalLight(0xffffff, 0.6), state.forceGraph]).hoverOrderComparator(function (a, b) {
      // Prioritize graph objects
      var aObj = getGraphObj(a);
      if (!aObj) return 1;
      var bObj = getGraphObj(b);
      if (!bObj) return -1;

      // Prioritize nodes over links
      var isNode = function isNode(o) {
        return o.__graphObjType === 'node';
      };
      return isNode(bObj) - isNode(aObj);
    }).tooltipContent(function (obj) {
      var graphObj = getGraphObj(obj);
      return graphObj ? accessorFn(state[graphObj.__graphObjType + 'Label'])(graphObj.__data) || '' : '';
    }).onHover(function (obj) {
      // Update tooltip and trigger onHover events
      var hoverObj = getGraphObj(obj);

      if (hoverObj !== state.hoverObj) {
        var prevObjType = state.hoverObj ? state.hoverObj.__graphObjType : null;
        var prevObjData = state.hoverObj ? state.hoverObj.__data : null;
        var objType = hoverObj ? hoverObj.__graphObjType : null;
        var objData = hoverObj ? hoverObj.__data : null;
        if (prevObjType && prevObjType !== objType) {
          // Hover out
          state['on' + (prevObjType === 'node' ? 'Node' : 'Link') + 'Hover'](null, prevObjData);
        }
        if (objType) {
          // Hover in
          state['on' + (objType === 'node' ? 'Node' : 'Link') + 'Hover'](objData, prevObjType === objType ? prevObjData : null);
        }

        state.hoverObj = hoverObj;
      }
    }).onClick(function (obj) {
      // Handle click events on objects
      if (state.ignoreOneClick) {
        // f.e. because of dragend event
        state.ignoreOneClick = false;
        return;
      }

      var graphObj = getGraphObj(obj);
      if (graphObj) {
        state['on' + (graphObj.__graphObjType === 'node' ? 'Node' : 'Link') + 'Click'](graphObj.__data);
      }
    });

    //

    // Kick-off renderer
    (function animate() {
      // IIFE
      if (state.enablePointerInteraction) {
        // reset canvas cursor (override dragControls cursor)
        renderer.domElement.style.cursor = null;
      }

      // Frame cycle
      state.forceGraph.tickFrame();
      state.renderObjs.tick();
      state.animationFrameRequestId = requestAnimationFrame(animate);
    })();
  }
});

module.exports = _3dForceGraph;
