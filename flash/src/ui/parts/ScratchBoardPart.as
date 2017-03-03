// ScratchBoardPart.as
// Nikita Yaschenko, 2014
//
// Shows data from robot's analog sensors

package ui.parts{
   import flash.display.Graphics;
   import flash.display.Shape;
   import flash.text.TextField;
   import flash.text.TextFormat;
   import flash.display.Sprite;

   import translation.Translator;

   public class ScratchBoardPart extends UIPart {
      private const stageAreaWidth:int = 50;
      private const titleTextFormat:TextFormat = new TextFormat(CSS.font, 10, CSS.textColor, true);
      private const dataTextFormat:TextFormat  = new TextFormat(CSS.font, 10, CSS.textColor);

      private static const TITLES:Array = new Array(
//          "Encoder L",
//          "Encoder R",
            "Trip meter L",
            "Trip meter R",
            "Sensor 1",
            "Sensor 2",
            "Sensor 3",
            "Sensor 4",
            "Sensor 5",
            "Start button"
//            "Finished?"
      );


      private const DEFAULT_VALUE:String = '';

      private const TEXT_TITLE_WIDTH:int = 75;
      private const TEXT_TITLE_X:int = 5;        // x-coordinate of labels for @TITLES strings
      private const TEXT_DATA_X:int = TEXT_TITLE_X + TEXT_TITLE_WIDTH;        // x-coordinate of labels for sensors' data
                                                 // maybe you need to change it if add new titles in @TITLES
      private const TEXT_Y:int = 32;
      private const TEXT_VERTICAL_STEP:int = 17;

      private const TEXT_TITLE_X_LARGE_STAGE:int = TEXT_DATA_X;
      private const TEXT_DATA_X_LARGE_STAGE:int = 5 + TEXT_TITLE_X_LARGE_STAGE + TEXT_TITLE_WIDTH;

      private var shape:Shape;
      private var spritesTitle:TextField;

      private var titleLabels:Array;
      private var dataLabels:Array;
      private var dataSprites:Array;

      public var arraySensors:Array = new Array();


      public function ScratchBoardPart(app:Scratch) {
         this.app = app;


         addChild(shape = new Shape());

         titleLabels = new Array();
         dataLabels = new Array();
         dataSprites = new Array();

         for (var i:int = 0; i < TITLES.length; i++) {
            var deviceSelector:DeviceSelector = new DeviceSelector(app, i + 1, 0);
            arraySensors[i] = deviceSelector;
            deviceSelector.x = 102;
            deviceSelector.y = 65 + i * 17;
            addChild(deviceSelector);

            var str:String = TITLES[i];
            var label:TextField;

            label = makeLabel(str + ':', titleTextFormat, TEXT_TITLE_X, TEXT_Y + i * TEXT_VERTICAL_STEP);
            titleLabels.push(label);
            addChild(label);

            label = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + i * TEXT_VERTICAL_STEP);
//            dataLabels.push(label);

            var sprite:Sprite = new Sprite();
            dataSprites[i] = sprite;
            sprite.addChild(label);

            addChild(sprite);
         }

         spritesTitle = makeLabel(Translator.map('Robot'), CSS.titleFormat, 10, 5);
         addChild(spritesTitle);
      }

      public function setWidthHeight(w:int, h:int):void {
         this.w = w;
         this.h = h;
         var g:Graphics = shape.graphics;
         g.clear();
         drawTopBar(g, CSS.titleBarColors, getTopBarPath(w, CSS.titleBarH), w, CSS.titleBarH);

         g.lineStyle(1, CSS.borderColor, 1, true);
         g.drawRect(0, CSS.titleBarH, w, h - CSS.titleBarH);
         g.endFill()
         fixLayout();
         if (app.viewedObj()) refresh(); // refresh, but not during initialization
      }

      public function setText(index:int, text:String):void {
         trace("index=" + index + " text=" + text);
         if (index < 0 || index >= TITLES.length) return;

         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         var label:TextField = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP);
         label.text = text;
         dataSprites[index].addChild(label);
      }
      public function disable(index:int):void {
         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         var label:TextField = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP);
         label.text = "--";
         dataSprites[index].addChild(label);
      }
      public function setColor(index:int, color:int):void {
         trace("color=" + color);

         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         var sprite:Sprite = new Sprite();

         sprite.graphics.beginFill(color);
         sprite.graphics.drawRect(TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP + 2, 11, 11);
         sprite.graphics.endFill();
         dataSprites[index].addChild(sprite);
      }

      private function fixLayout():void {
/*
         if (app.stageIsContracted) {//small stage
            for (var i:int = 0; i < TITLES.length; i++) {
               titleLabels[i].x = TEXT_TITLE_X;
               titleLabels[i].y = TEXT_Y + i * TEXT_VERTICAL_STEP;
               dataLabels[i].x = TEXT_DATA_X;
               dataLabels[i].y = TEXT_Y + i * TEXT_VERTICAL_STEP;
            }
            spritesTitle.x = 10;
            spritesTitle.y = 5;
         } else {
            var rBound:int = (TITLES.length + 1) / 2;
            var len:int = TITLES.length;
            for (var j:int = 0; j < rBound; j++) {
               titleLabels[i].x = TEXT_TITLE_X;
               titleLabels[i].y = TEXT_Y + j * TEXT_VERTICAL_STEP;
               dataLabels[i].x = TEXT_DATA_X;
               dataLabels[i].y = TEXT_Y + j * TEXT_VERTICAL_STEP;
            }
            for (var h:int = rBound; h < len; ++h) {
               titleLabels[i].x = TEXT_TITLE_X_LARGE_STAGE + titleLabels[h - rBound].x;
               titleLabels[i].y = TEXT_Y + (h - rBound) * TEXT_VERTICAL_STEP;
               dataLabels[i].x = TEXT_DATA_X_LARGE_STAGE;
               dataLabels[i].y = TEXT_Y + (h - rBound) * TEXT_VERTICAL_STEP;
            }
            spritesTitle.x = 10;
            spritesTitle.y = 5;
         }
*/
      }

      public function refresh():void {
      }

      public function updateTranslation():void {
         spritesTitle.text = Translator.map('Robot');
         for (var i:int = 0; i < titleLabels.length; ++i)
            (titleLabels[i] as TextField).text = Translator.map(TITLES[i]) + ":";
      }
   }
}