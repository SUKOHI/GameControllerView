package com.sukohi.lib;

/*** Requirements : com.sukohi.lib.CoodinateInRange ***/

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

public class GameControllerView extends View {
	
	public int KEY_LEFT = 0;
	public int KEY_UP = 1;
	public int KEY_RIGHT = 2;
	public int KEY_DOWN = 3;
	public int KEY_A = 4;
	public int KEY_B = 5;
	public int KEY_Y = 6;
	public int KEY_X = 7;
	public int KEY_L = 8;
	public int KEY_R = 9;
	public int KEY_SELECT = 10;
	public int KEY_START = 11;
	private String[] keyNames = {
			"left", "up", "right", "down", "A", "B", "Y", "X", "L", "R", "select", "start"
	};
	private HashMap<String, int[]> buttonSizes = new HashMap<String, int[]>();
	private SparseArray<RectF> keyRectFs = new SparseArray<RectF>();
	private SparseBooleanArray keyStatuses = new SparseBooleanArray();
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF rectF = new RectF();
	private int viewWidth, viewHeight, controllerPadding, crossKeyRepeatInterval;
	private int keyColor = Color.parseColor("#88FFFFFF");
	private GameControllerViewCallback gameControllerViewCallback;
	private Timer timer;
	private Handler handler = new Handler();
	
	public GameControllerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		int crossKeyWidth = attrs.getAttributeIntValue(null, "crossKeyWidth", 300);
		int crossKeyHeight = attrs.getAttributeIntValue(null, "crossKeyHeight", 300);
		setCrossKeySize(crossKeyWidth, crossKeyHeight);

		int ABYXKeyWidth = attrs.getAttributeIntValue(null, "ABYXKeyWidth", 100);
		int ABYXKeyHeight = attrs.getAttributeIntValue(null, "ABYXKeyHeight", 100);
		setABYXKeySize(ABYXKeyWidth, ABYXKeyHeight);

		int LRKeyWidth = attrs.getAttributeIntValue(null, "LRKeyWidth", 150);
		int LRKeyHeight = attrs.getAttributeIntValue(null, "LRKeyHeight", 65);
		setLRKeySize(LRKeyWidth, LRKeyHeight);
		
		int selectStartKeyWidth = attrs.getAttributeIntValue(null, "selectStartKeyWidth", 70);
		int selectStartKeyHeight = attrs.getAttributeIntValue(null, "selectStartKeyHeight", 30);
		setSelectStartKeySize(selectStartKeyWidth, selectStartKeyHeight);
		
		int controllerPadding = attrs.getAttributeIntValue(null, "controllerPadding", 15);
		setControllerPadding(controllerPadding);
		
		int repeatInterval = attrs.getAttributeIntValue(null, "crossKeyRepeatInterval", 500);
		setCrossKeyRepeatInterval(repeatInterval);
		
		String keyColorAttr = attrs.getAttributeValue(null, "keyColor");
		
		if(keyColorAttr != null) {
			
			setKeyColor(keyColorAttr);
			
		}
		
        textPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        
        for(int i = 0 ; i < keyNames.length ; i++) {
        	
        	keyStatuses.put(i, false);
        	
        }
        
	}
	
	public void setCrossKeySize(int width, int height) {
		
		setKeySize("cross", width, height);
		
	}
	
	public int[] getCrossKeySize() {
		
		return getKeySize("cross");
		
	}
	
	public void setSelectStartKeySize(int width, int height) {
		
		setKeySize("selectStart", width, height);
		
	}
	
	public int[] getSelectStartKeySize() {
		
		return getKeySize("selectStart");
		
	}
	
	public void setABYXKeySize(int width, int height) {
		
		setKeySize("ABYX", width, height);
		
	}
	
	public int[] getABYXKeySize() {
		
		return getKeySize("ABYX");
		
	}
	
	public void setLRKeySize(int width, int height) {
		
		setKeySize("LR", width, height);
		
	}
	
	public int[] getLRKeySize() {
		
		return getKeySize("LR");
		
	}
	
	public void setKeySize(String key, int width, int height) {
		
		int[] size = {width, height};
		buttonSizes.put(key, size);
		
	}
	
	public int[] getKeySize(String key) {
		
		return buttonSizes.get(key);
		
	}
	
	public void setControllerPadding(int padding) {
		
		controllerPadding = padding;
		
	}
	
	public void setKeyColor(String keyColorAttr) {
		
		keyColor = Color.parseColor(keyColorAttr);
		
	}
	
	public void setCrossKeyRepeatInterval(int milliseconds) {
		
		if(timer != null) {
			
			timer.cancel();
			
		}
		
		crossKeyRepeatInterval = milliseconds;
		crossKeyRepeat();
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int[] ABYXKeySize = getABYXKeySize();
		int ABYXKeyHeight = ABYXKeySize[1];
		int ABYXKeyMeasureHeight = ABYXKeyHeight * 3;
		
		int[] LRKeySize = getLRKeySize();
		int LRKeyHeight = LRKeySize[1];
		int ABYXLRKeyMeasureHeight = (int) (ABYXKeyMeasureHeight + (LRKeyHeight*1.5));
		
		int baseHeight = (int) (ABYXKeyMeasureHeight * 0.5);
		
		int[] selectStartKeySize = getSelectStartKeySize();
		int selectStartKeyHeight = selectStartKeySize[1];
		int selectStartKeyHalfHeight = (int) (selectStartKeyHeight * 0.5);
		int selectStartKeyMeasureHeight = baseHeight + selectStartKeyHalfHeight;
		
		int[] crossKeySize = getCrossKeySize();
		int crossKeyHeight = crossKeySize[1];
		int crossKeyHalfHeight = (int) (crossKeyHeight * 0.5);
		int crossKeyMeasureHeight = baseHeight + crossKeyHalfHeight;
		
		int height = Math.max(ABYXLRKeyMeasureHeight, selectStartKeyMeasureHeight);
		viewHeight = Math.max(height, crossKeyMeasureHeight) + controllerPadding*2;
		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(viewWidth , viewHeight);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		paint.setStyle(Paint.Style.FILL);
        paint.setColor(keyColor);

		int i;
		
        // Cross
        
        int[] crossKeySize = getCrossKeySize();
        int crossKeyWidth = crossKeySize[0];
        int crossKeyPieceWidth = crossKeyWidth / 3;
		int crossKeyHeight = crossKeySize[1];
		int crossKeyPieceHeight = crossKeyHeight / 3;
		
		float crossBaseX = crossKeyWidth / 2 + controllerPadding;
		float crossBaseY = viewHeight - crossKeyHeight*0.5F - controllerPadding;

		float[][] crossDifferences = {
				{-1.5F, -0.5F, -0.5F, 0.5F}, 
				{-0.5F, -1.5F, 0.5F, -0.5F}, 
				{0.5F, -0.5F, 1.5F, 0.5F}, 
				{-0.5F, 0.5F, 0.5F, 1.5F}
		};
		float[] crossDifference;
		int[] crossKeyNumbers = {KEY_LEFT, KEY_UP, KEY_RIGHT, KEY_DOWN};
		
		for(i = 0 ; i < crossDifferences.length ; i++) {
			
			crossDifference = crossDifferences[i];
			rectF.set(
					(crossBaseX+crossKeyPieceWidth*crossDifference[0]), 
					(crossBaseY+crossKeyPieceHeight*crossDifference[1]), 
					(crossBaseX+crossKeyPieceWidth*crossDifference[2]), 
					(crossBaseY+crossKeyPieceHeight*crossDifference[3])
			);
			setKeyRectF(crossKeyNumbers[i], rectF);
			canvas.drawRoundRect(rectF, 20, 20, paint);
			
		}
        
		// ABYX
		 
		int[] ABYXKeySize = getABYXKeySize();
		int ABYXKeyWidth = ABYXKeySize[0];
		int ABYXKeyHeight = ABYXKeySize[1];
		
		float ABYXBaseX = viewWidth - ABYXKeyWidth *1.5F - controllerPadding;
		float ABYXBaseY = viewHeight - ABYXKeyHeight *1.5F - controllerPadding;
		
		float[][] ABYXDifferences = {
				{0.5F, -0.5F, 1.5F, 0.5F}, 
				{-0.5F, 0.5F, 0.5F, 1.5F}, 
				{-1.5F, -0.5F, -0.5F, 0.5F}, 
				{-0.5F, -1.5F, 0.5F, -0.5F}
		};
		float[] ABYXDifference;
		int[] ABYXKeyNumbers = {KEY_A, KEY_B, KEY_Y, KEY_X};
		
		for(i = 0 ; i < ABYXDifferences.length ; i++) {
			
			ABYXDifference = ABYXDifferences[i];
			rectF.set(
					(ABYXBaseX+ABYXKeyWidth*ABYXDifference[0]), 
					(ABYXBaseY+ABYXKeyHeight*ABYXDifference[1]), 
					(ABYXBaseX+ABYXKeyWidth*ABYXDifference[2]), 
					(ABYXBaseY+ABYXKeyHeight*ABYXDifference[3])
			);
			setKeyRectF(ABYXKeyNumbers[i], rectF);
			canvas.drawOval(rectF, paint);
			
		}

		// LR
		
		int[] LRKeySize = getLRKeySize();
		int LRKeyWidth = LRKeySize[0];
		int LRKeyHeight = LRKeySize[1];
		
		float[][] LRBaseXYs = {
				{LRKeyWidth*0.75F+controllerPadding, LRKeyHeight*0.5F+controllerPadding}, 
				{viewWidth-LRKeyWidth*0.75F+controllerPadding, LRKeyHeight*0.5F+controllerPadding}
		};
		float[] LRBaseXY;
		int[] LRKeyNumbers = {KEY_L, KEY_R};
		
		for(i = 0 ; i < LRBaseXYs.length ; i++) {
			
			LRBaseXY = LRBaseXYs[i];
			rectF.set(
					(LRBaseXY[0]-LRKeyWidth*0.5F), 
					(LRBaseXY[1]-LRKeyHeight*0.5F),
					(LRBaseXY[0]+LRKeyWidth*0.5F), 
					(LRBaseXY[1]+LRKeyHeight*0.5F)
			);
			setKeyRectF(LRKeyNumbers[i], rectF);
	        canvas.drawRoundRect(rectF, 20, 20, paint);
			
		}
		
		// Select, Start
		
		int[] SSKeySize = getSelectStartKeySize();
		int SSKeyWidth = SSKeySize[0];
		int SSKeyHeight = SSKeySize[1];
		
		float[][] SSBaseXYs = {
				{viewWidth*0.5F-SSKeyWidth*0.9F+controllerPadding, viewHeight-SSKeyHeight*0.5F-controllerPadding}, 
				{viewWidth*0.5F+SSKeyWidth*0.9F-controllerPadding, viewHeight-SSKeyHeight*0.5F-controllerPadding} 
		};
		float[] SSBaseXY;
		int[] SSKeyNumbers = {KEY_SELECT, KEY_START};
		
		for(i = 0 ; i < SSBaseXYs.length ; i++) {
			
			SSBaseXY = SSBaseXYs[i];
			rectF.set(
					(SSBaseXY[0]-SSKeyWidth*0.5F), 
					(SSBaseXY[1]-SSKeyHeight*0.5F),
					(SSBaseXY[0]+SSKeyWidth*0.5F), 
					(SSBaseXY[1]+SSKeyHeight*0.5F)
			);
			setKeyRectF(SSKeyNumbers[i], rectF);
	        canvas.drawRoundRect(rectF, 10, 10, paint);
			
		}
		
        // Text
		
		float textSize, textWidth, baseX, baseY;
		String text;
		FontMetrics fontMetrics;
		
		textPaint.setColor(Color.TRANSPARENT);
		textSize = Math.min(ABYXKeyWidth, ABYXKeyHeight) * 0.5F;
        textPaint.setTextSize(textSize);
        fontMetrics = textPaint.getFontMetrics();
        
        float[][] textDifferences = {
        		{(ABYXBaseX+ABYXKeyWidth), ABYXBaseY}, 
        		{ABYXBaseX, (ABYXBaseY+ABYXKeyHeight)}, 
        		{(ABYXBaseX-ABYXKeyWidth), ABYXBaseY}, 
        		{ABYXBaseX, (ABYXBaseY-ABYXKeyHeight)}
        };
        float textX, textY;
        
        for(i = 0 ; i < ABYXKeyNumbers.length ; i++) {
        	
        	text = keyNames[ABYXKeyNumbers[i]];
        	textWidth = textPaint.measureText(text);
        	baseX = textDifferences[i][0];
        	baseY = textDifferences[i][1];
        	textX = baseX - textWidth / 2;
        	textY = baseY - (fontMetrics.ascent + fontMetrics.descent) / 2;
        	canvas.drawText(text, textX, textY, textPaint);
        	
        }
        
        textSize = Math.min(LRKeyWidth, LRKeyHeight) * 0.7F;
        textPaint.setTextSize(textSize);
        fontMetrics = textPaint.getFontMetrics();
        
        for(i = 0 ; i < LRKeyNumbers.length ; i++) {
        	
        	text = keyNames[LRKeyNumbers[i]];
        	textWidth = textPaint.measureText(text);
        	textX = LRBaseXYs[i][0] - textWidth / 2;
        	textY = LRBaseXYs[i][1] - (fontMetrics.ascent + fontMetrics.descent) / 2;
        	canvas.drawText(text, textX, textY, textPaint);
        	
        }
        
        textSize = Math.min(SSKeyWidth, SSKeyHeight) * 0.8F;
        textPaint.setTextSize(textSize);
        
        fontMetrics = textPaint.getFontMetrics();
        
        for(i = 0 ; i < SSKeyNumbers.length ; i++) {
        	
        	text = keyNames[SSKeyNumbers[i]];
        	textWidth = textPaint.measureText(text);
        	textX = SSBaseXYs[i][0] - textWidth / 2;
        	textY = SSBaseXYs[i][1] - (fontMetrics.ascent + fontMetrics.descent) / 2;
        	canvas.drawText(text, textX, textY, textPaint);
        	
        }
        
	}

	public void setCallback(GameControllerViewCallback callback) {
		
		gameControllerViewCallback = callback;
		
	}
	
	private void setKeyRectF(int keyNumber, RectF rectF) {
		
		RectF keyNameRectF = new RectF();
		keyNameRectF.set(rectF);
		keyRectFs.put(keyNumber, keyNameRectF);
		
	}
	
	private int getKeyWithCoodinate(float x, float y) {
		
		CoodinateInRange coodinateInRange = new CoodinateInRange();
		coodinateInRange.setCheckXY(x, y);
		
		for(int i = 0 ; i < keyRectFs.size() ; i++) {
			
			int keyNumber = keyRectFs.keyAt(i);
			RectF keyRectF = keyRectFs.get(keyNumber);
			
			if((keyNumber == KEY_LEFT || keyNumber == KEY_UP || keyNumber == KEY_RIGHT || keyNumber == KEY_DOWN) 
					&& coodinateInRange.inRangeRectangleCornerRadius(keyRectF, 20, 20)) {
				
				return keyNumber;
				
			} else if((keyNumber == KEY_A || keyNumber == KEY_B || keyNumber == KEY_Y || keyNumber == KEY_X)
					&& coodinateInRange.inRangeCircle(keyRectF, getABYXKeySize()[0])) {
				
				return keyNumber;
				
			} else if((keyNumber == KEY_L || keyNumber == KEY_R)
					&& coodinateInRange.inRangeRectangleCornerRadius(keyRectF, 20, 20)) {
				
				return keyNumber;
					
			} else if((keyNumber == KEY_SELECT || keyNumber == KEY_START)
					&& coodinateInRange.inRangeRectangleCornerRadius(keyRectF, 10, 10)) {
				
				return keyNumber;
					
			}
			
		}
		
		return -1;
		
	}
	
	private SparseIntArray pointerIds = new SparseIntArray();
	
	private void setKeyNumberOnPointerId(int pointerId, int keyNumber) {
		
		pointerIds.put(pointerId, keyNumber);
		
	}
	
	private void removeKeyNumberOnPointerId(int pointerId) {
		
		pointerIds.delete(pointerId);
		
	}
	
	private int getKeyNumberOnPointerId(int pointerId) {
		
		return pointerIds.get(pointerId, -1);
		
	}
	
	private int getKeyNumber(float x, float y) {
		
		return getKeyWithCoodinate(x, y);
		
	}
	
	private void dispatchOnCallback(int keyNumber, int pointerId) {
		
		if(keyNumber != -1) {
		
			setKeyStatus(keyNumber, true);
			setKeyNumberOnPointerId(pointerId, keyNumber);
			gameControllerViewCallback.on(keyNumber, keyNames[keyNumber], keyStatuses);
		
		}
		
	}
	
	private void dispatchOffCallback(int keyNumber, int pointerId) {
		
		if(keyNumber != -1) {
		
			setKeyStatus(keyNumber, false);
			removeKeyNumberOnPointerId(pointerId);
			gameControllerViewCallback.off(keyNumber, keyNames[keyNumber], keyStatuses);
		
		}
		
	}
	
	private void setKeyStatus(int keyNumber, boolean onFlag) {
		
		keyStatuses.put(keyNumber, onFlag);
		
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	int action = event.getAction();
    	int actionIndex = event.getActionIndex();
    	int pointerId = event.getPointerId(actionIndex);
    	int keyNumber;
    	
    	@SuppressWarnings("deprecation")
		int id = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        float x = 0;
        float y = 0;
        
        switch(action & MotionEvent.ACTION_MASK) {
 
        case MotionEvent.ACTION_DOWN:
        	x = event.getX();
            y = event.getY();
            keyNumber = getKeyNumber(x, y);
    		dispatchOnCallback(keyNumber, pointerId);
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            x = event.getX(id);
            y = event.getY(id);
            keyNumber = getKeyNumber(x, y);
    		dispatchOnCallback(keyNumber, pointerId);
            break;
        case MotionEvent.ACTION_UP:
        	keyNumber = getKeyNumberOnPointerId(pointerId);
        	dispatchOffCallback(keyNumber, pointerId);
            break;
        case MotionEvent.ACTION_POINTER_UP:
        	keyNumber = getKeyNumberOnPointerId(pointerId);
        	dispatchOffCallback(keyNumber, pointerId);
            break;
        case MotionEvent.ACTION_MOVE:
        	
            int pointerCount = event.getPointerCount();
            
            for(int i = 0; i < pointerCount; i++) {
            	
                pointerId = event.getPointerId(i);
                x = event.getX(i);
                y = event.getY(i);
               
                if(pointerIds.get(pointerId) != -1) {
                	
                	if(getKeyNumber(x, y) == -1) {
                		
                		keyNumber = getKeyNumberOnPointerId(pointerId);
                    	dispatchOffCallback(keyNumber, pointerId);
                		
                	}
                	
                }

                if(pointerIds.get(pointerId, -1) == -1) {
                	
                	keyNumber = getKeyNumber(x, y);
                	
                	if(keyNumber != -1) {
                		
                		dispatchOnCallback(keyNumber, pointerId);
                		
                	}
                	
                }
                
            }
            
            break;
        }

        return true;
        
    }
    
	private void crossKeyRepeat() {
		
		if(crossKeyRepeatInterval > 0) {
		
	        timer = null;
	        timer = new Timer();
	        timer.schedule(new TimerTask() {
	
	            @Override
	            public void run() {
	                
					handler.post(new Runnable() {
	                    @Override
	                    public void run() {
	                    	
	                    	for(int i = 0 ; i < keyStatuses.size() ; i++) {
	                    		
	                    		int keyNumber = keyStatuses.keyAt(i);
	                    		
	                    		if(keyNumber == KEY_LEFT
	                    				|| keyNumber == KEY_UP
	                    				|| keyNumber == KEY_RIGHT
	                    				|| keyNumber == KEY_DOWN) {
	                    			
	                    			if(keyStatuses.get(keyNumber)) {
	                    				
	                    				gameControllerViewCallback.on(keyNumber, keyNames[keyNumber], keyStatuses);
	                    				
	                    			}
	                    			
	                    		}
	                    		
	                    	}
	                    	
	                    }
	                });
					
	            }
	
	        }, crossKeyRepeatInterval, crossKeyRepeatInterval);
        
		}
        
	}

    public static class GameControllerViewCallback {
    	
    	public void on(int keyNumber, String keyName, SparseBooleanArray keyStatuses) {}
    	public void off(int keyNumber, String keyName, SparseBooleanArray keyStatuses) {}
    	
    }

}

/***Sample

	GameControllerView gameControllerView = (GameControllerView) findViewById(R.id.game_controller_view);
	gameControllerView.setCallback(new GameControllerViewCallback(){
		
		public void on(int key, String keyName, SparseBooleanArray keyStatuses) {
			
			Log.v("on", ""+ key +": "+ keyName);
			
		}
		
    	public void off(int key, String keyName, SparseBooleanArray keyStatuses) {
    		
    		Log.v("off", ""+ key +": "+ keyName);
    		
    	}
		
	});
	gameControllerView.setCrossKeySize(300, 300);		// Optional: Size of cross keys
	gameControllerView.setABYXKeySize(100, 100);		// Optional: Size of ABYX keys
	gameControllerView.setLRKeySize(150, 65);			// Optional: Size of LR keys
	gameControllerView.setSelectStartKeySize(70, 30);	// Optional: Size of Select and Start keys
	gameControllerView.setControllerPadding(15);		// Optional: Padding value of the controller
	gameControllerView.setCrossKeyRepeatInterval(500);	// Optional: Milliseconds that repeatedly dispath callback "on" of left, up, right and down keys. If 0, repeat is unavailable.
	gameControllerView.setKeyColor("#88FFFFFF");		// Optional: Color of all keys

	// *Note: The following are available as attributes of xml tag.
	// crossKeyWidth, crossKeyHeight, ABYXKeyWidth, ABYXKeyHeight, LRKeyWidth, LRKeyHeight, 
	// selectStartKeyWidth, selectStartKeyHeight, controllerPadding, crossKeyRepeatInterval, keyColor

***/