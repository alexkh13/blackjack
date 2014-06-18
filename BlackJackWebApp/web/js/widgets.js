var WidgetFactory = {
    _widgets: {},

    get: function(widget) {
        return this._widgets[widget].apply(this,[].slice.call(arguments).splice(1));
    }
};

(function(WidgetFactory) {

    function generate(widget) {
        return $("#"+widget).clone().show();
    }

    WidgetFactory._widgets['betSlider'] = function(startAmount, callback) {
        var element = generate("betContainer");
        element.children("#betAmount").text("\uf155" + startAmount);
        element.children("#betSlider").slider({
            range: "min",
            value: startAmount,
            min: 50,
            max: 1000,
            step: 50,
            slide: function( event, ui ) {
                element.children("#betAmount").text("\uf155" + ui.value);
            }
        });
        element.children("#betTrigger").bind("click",function() {
            callback(element.children("#betSlider").slider("value"));
        });
        return element;
    }

    WidgetFactory._widgets['actionPanel'] = function(callback) {
        var element = generate("actionContainer");
        element.children("#hitButton").bind("click",function() {
            callback('HIT');
        });
        element.children("#standButton").bind("click",function() {
            callback('STAND');
        });
        element.children("#splitButton").bind("click",function() {
            callback('SPLIT');
        });
        element.children("#doubleButton").bind("click",function() {
            callback('DOUBLE');
        });
        return element;
    }

    WidgetFactory._widgets['actionTimer'] = function(timeout, callback) {

    }

})(WidgetFactory);