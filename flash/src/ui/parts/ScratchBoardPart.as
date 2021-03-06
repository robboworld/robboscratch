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
   import flash.events.MouseEvent;
   import uiwidgets.DialogBox;

   import translation.Translator;
   import uiwidgets.BlockColorSensorCorrector;


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
      private var dataSprites:Array;

      public var arrSensorSelectors:Array = new Array();
      public var arrSensorCheckboxes:Array = new Array();

      private var labelLeftMotor:TextField;
      private var labelRightMotor:TextField;
      private var labelStartButton:TextField;

      private var connected:Boolean = false;


   private function pizda(iSlot:int):Function{
      return function(e:MouseEvent):void{
         trace("Show corrector for slot=" + iSlot);

         var d:DialogBox = new DialogBox();
         d.addTitle('Color Sensor Correction ' + (iSlot + 1));
         app.arrayBlockColorSensorCorrector[iSlot] = new BlockColorSensorCorrector(app, iSlot);
         d.addWidget(app.arrayBlockColorSensorCorrector[iSlot]);
         d.addButton('Close', d.cancel);
         d.showOnStage(app.stage, true);
      }
   }



      public function ScratchBoardPart(app:Scratch) {
         this.app = app;

         addChild(shape = new Shape());

         titleLabels = new Array();
         dataSprites = new Array();

         for (var i:int = 0; i < TITLES.length; i++) {
            var str:String = TITLES[i];
            var label:TextField;

            label = makeLabel(str + ':', titleTextFormat, TEXT_TITLE_X, TEXT_Y + i * TEXT_VERTICAL_STEP);
            titleLabels.push(label);
            addChild(label);

            var sprite:Sprite = new Sprite();
            dataSprites[i] = sprite;

            sprite.addEventListener(MouseEvent.CLICK, pizda(i));


            addChild(sprite);
         }


         labelLeftMotor = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + 0  * TEXT_VERTICAL_STEP);
         addChild(labelLeftMotor);
         labelRightMotor = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + 1 * TEXT_VERTICAL_STEP);
         addChild(labelRightMotor);
         labelStartButton = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + 7 * TEXT_VERTICAL_STEP);
         addChild(labelStartButton);


         spritesTitle = makeLabel(Translator.map('Robot'), CSS.titleFormat, 6, 5);
         addChild(spritesTitle);


         this.setExtendedMode(false);
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
         showConenctionIcon();

         if (app.viewedObj()) refresh(); // refresh, but not during initialization
      }


      public function showConenctionIcon():void{
         var g:Graphics = shape.graphics;

         if(connected){
            g.beginFill(0x00FF00);
         }
         else{
            g.beginFill(0xFF0000);
         }
         g.lineStyle(0, 0, 0);

         TARGET::desktop{
            g.drawCircle(112, 16, 5);
         }
         TARGET::android{
            g.drawRect(108,12,8,8);
         }
         g.endFill();

      }



      public function setConnected(status:Boolean):void{
         if(connected != status){
            connected = status;
            showConenctionIcon();

            //Redraw lab blocks in case version is changed
            if(app.scriptsPart.selector.selectedCategory == 13){
               app.getPaletteBuilder().showBlocksForCategory(13, true, false);
            }
         }
      }




      public function setTextValue(index:int, text:String):void {
         trace("index=" + index + " text=" + text);
         if (index < 0 || index >= TITLES.length) return;

         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         dataSprites[index].graphics.clear();
         var label:TextField = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP);
         label.text = text;
         dataSprites[index].addChild(label);
      }
      public function disableValue(index:int):void {
         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         dataSprites[index].graphics.clear();
         var label:TextField = makeLabel(DEFAULT_VALUE, dataTextFormat, TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP);
         label.text = "--";
         dataSprites[index].addChild(label);
      }
      public function setColorValue(index:int, color:int):void {
         trace("color=" + color);

         while (dataSprites[index].numChildren > 0) dataSprites[index].removeChildAt(0);

         //var sprite:Sprite = new Sprite();

         //Border
         dataSprites[index].graphics.beginFill(0x666666, 1);
         dataSprites[index].graphics.drawRect(TEXT_DATA_X, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP + 2, 11, 11);
         dataSprites[index].graphics.endFill();


         dataSprites[index].graphics.beginFill(color);
         dataSprites[index].graphics.drawRect(TEXT_DATA_X + 1, TEXT_Y + (index + 2) * TEXT_VERTICAL_STEP + 3, 9, 9);
         dataSprites[index].graphics.endFill();

//         dataSprites[index].addChild(sprite);
      }



      public function setLeftPath(path:int):void {
         labelLeftMotor.text = path.toString();
      }
      public function setLeftPathDisabled():void {
         labelLeftMotor.text = "--";
      }
      public function setRightPath(path:int):void {
         labelRightMotor.text = path.toString();
      }
      public function setRightPathDisabled():void {
         labelRightMotor.text = "--";
      }





      public function setStartButton(state:Boolean):void{
         if(state){
            labelStartButton.text = Translator.map("true");
         }
         else{
            labelStartButton.text = Translator.map("false");
         }
      }


      public function setExtendedMode(isEnabled:Boolean):void{
         if(isEnabled){
            for (var f:int = 0; f < Scratch.ROBOT_SENSOR_COUNT; f++) {
               if(arrSensorCheckboxes[f] != null && contains(arrSensorCheckboxes[f])){
                  removeChild(arrSensorCheckboxes[f]);
               }

               var deviceSelector:DeviceSelector = new DeviceSelector(app, f + 1, 0);
               arrSensorSelectors[f] = deviceSelector;
               deviceSelector.x = 102;
               deviceSelector.y = 65 + f * 17;
               addChild(deviceSelector);
            }
         }
         else{
            for (var h:int = 0; h < Scratch.ROBOT_SENSOR_COUNT; h++) {
               if(arrSensorSelectors[h] != null && contains(arrSensorSelectors[h])){
                  removeChild(arrSensorSelectors[h]);
               }

               var cb:CheckBox = new CheckBox();
               cb.x = 104;
               cb.y = 67 + h * 17;
               arrSensorCheckboxes[h] = cb;
               addChild(cb);
            }
         }
      }


      public function setEncodersAvailable(isEnabled:Boolean):void{
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