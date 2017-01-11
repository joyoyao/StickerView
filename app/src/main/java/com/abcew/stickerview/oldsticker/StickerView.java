package com.abcew.stickerview.oldsticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * 贴图操作控件
 */
public class StickerView extends View {
  private static int STATUS_IDLE = 0;
  private static int STATUS_MOVE = 1;// 移动状态
  private static int STATUS_DELETE = 2;// 删除状态
  private static int STATUS_ROTATE = 3;// 图片旋转状态
  private static int STATUS_MODIFY_TEXT = 4;// 移动状态
  Handler handler = new Handler();
  private int imageCount;// 已加入照片的数量
  private Context mContext;
  private int currentStatus;// 当前状态
  private StickerItem currentItem;// 当前操作的贴图数据
  private float oldx, oldy;
  private float startx, starty;
  private Paint rectPaint = new Paint();
  private OnStickerDelateLister stickerDelateLister;
  private OnStickerClickLister stickerClickLister;
  private boolean IsEnable = true;

  private PaintFlagsDrawFilter pfd;
  private LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<Integer, StickerItem>();
      // 存贮每层贴图数据
  int clickCount = 0;

  public StickerView(Context context) {
    super(context);
    init(context);
  }

  public StickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public void setOnStickerClickLister(OnStickerClickLister stickerClickLister) {
    this.stickerClickLister = stickerClickLister;
  }

  public void setOnStickerDelateLister(OnStickerDelateLister stickerDelateLister) {
    this.stickerDelateLister = stickerDelateLister;
  }

  public boolean isEnable() {
    return IsEnable;
  }

  public void setEnable(boolean enable) {
    IsEnable = enable;
  }

  private void init(Context context) {
    this.mContext = context;
    pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG| Paint.FILTER_BITMAP_FLAG);
    currentStatus = STATUS_IDLE;
    rectPaint.setColor(Color.RED);
    rectPaint.setAlpha(100);
  }

  LinkedHashMap<Integer, String> mHashMap = new LinkedHashMap<>();

  public void addBitImage(final Bitmap addBit, int hashCode, String title) {
    if (currentItem != null) {
      currentItem.isDrawHelpTool = false;
    }
    mHashMap.put(hashCode, title);
    resetSelect();
    StickerImage item = new StickerImage(this.getContext());
    item.setHashCode(hashCode);
    item.init(addBit, this);
    bank.put(++imageCount, item);
    currentItem = item;
    this.invalidate();// 重绘视图
  }

  public void addBitImage(final Bitmap addBit, int hashCode, String title, String stickerId) {
    if (currentItem != null) {
      currentItem.isDrawHelpTool = false;
    }
    mHashMap.put(hashCode, title);
    resetSelect();
    StickerImage item = new StickerImage(this.getContext());
      item.setStickerId(stickerId);
    item.setHashCode(hashCode);
    item.init(addBit, this);
    bank.put(++imageCount, item);
    currentItem = item;
    this.invalidate();// 重绘视图
  }


  public void addBitImage(final Bitmap addBit, int hashCode, String title, int positioX, int positioY) {
    if (currentItem != null) {
      currentItem.isDrawHelpTool = false;
    }
    mHashMap.put(hashCode, title);
    resetSelect();
    StickerImage item = new StickerImage(this.getContext());
    item.setHashCode(hashCode);
    item.init(addBit, this,positioX,positioY);
    bank.put(++imageCount, item);
    currentItem = item;
    this.invalidate();// 重绘视图
  }

  public void addText(final String addBit, int hashCode) {
    if (currentItem != null) {
      currentItem.isDrawHelpTool = false;
    }
    resetSelect();
    StickerText item = new StickerText(this.getContext());
    item.setHashCode(hashCode);
    item.init(addBit, this);
    bank.put(++imageCount, item);
    currentItem = item;
    this.invalidate();// 重绘视图
  }

  public void addText(final String addBit, int hashCode, int positioX, int positioY) {
    if (currentItem != null) {
      currentItem.isDrawHelpTool = false;
    }
    resetSelect();
    StickerText item = new StickerText(this.getContext());
    item.setHashCode(hashCode);
    item.init(addBit, this);
    bank.put(++imageCount, item);
    currentItem = item;
    this.invalidate();// 重绘视图
  }

  private void resetSelect() {
    for (Integer id : bank.keySet()) {
      StickerItem item = bank.get(id);
      if (item.isDrawHelpTool = true) {
        item.isDrawHelpTool = false;
      }
    }
  }

  /**
   * 绘制客户页面
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.setDrawFilter(pfd);
    // System.out.println("on draw!!~");
    for (Integer id : bank.keySet()) {
      StickerItem item = bank.get(id);
      item.draw(canvas);
    }// end for each
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // System.out.println(w + "   " + h + "    " + oldw + "   " + oldh);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (!IsEnable) {
      return false;
    }

    boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗

    int action = event.getAction();
    float x = event.getX();
    float y = event.getY();
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        int deleteId = -1;
        int topId = -1;
        boolean isSelectSticker = false;
        for (Integer id : bank.keySet()) {
          StickerItem item = bank.get(id);
          if (item.detectDeleteRect.contains(x, y)) {// 删除模式
            // ret = true;
            deleteId = id;
            topId=-1;
            currentStatus = STATUS_DELETE;
            isSelectSticker = true;
          } else if (item.detectRotateRect.contains(x, y)) {// 点击了旋转按钮
            deleteId=-1;
            ret = true;
            if (currentItem != null) {
              currentItem.isDrawHelpTool = false;
            }
            currentItem = item;
            topId = id;
            currentItem.isDrawHelpTool = true;
            currentStatus = STATUS_ROTATE;
            oldx = x;
            oldy = y;
            isSelectSticker = true;
          } else if (item.dstRect.contains(x, y)) {// 移动模式
            deleteId=-1;
            // 被选中一张贴图
            ret = true;
            if (currentItem != null) {
              currentItem.isDrawHelpTool = false;
            }
            currentItem = item;
            topId = id;
            currentItem.isDrawHelpTool = true;
            currentStatus = STATUS_MOVE;
            oldx = x;
            oldy = y;
            invalidate();
            isSelectSticker = true;
          }// end if
        }// end for each

        startx = x;
        starty = y;

        if (isSelectSticker) {
          if (stickerClickLister != null) {
            if (currentItem instanceof StickerImage) {
              stickerClickLister.OnStickerClick();
            }
            //                        if(){
            //                            if (currentItem instanceof StickerText) {
            //                                stickerClickLister.OnStickerTextClick((StickerText) currentItem);
            //                            }
            //                        }

          }
        }
        if (currentItem != null && currentStatus == STATUS_IDLE) {// 没有贴图被选择
          currentItem.isDrawHelpTool = false;
          if (currentItem instanceof StickerText) {
            stickerClickLister.OnHiddenStickerTextPanel();
          }
          currentItem = null;
          //				resetSelect();
          invalidate();
        }

        if (topId > 0 && topId <imageCount && currentItem.isDrawHelpTool == true&&currentStatus!=STATUS_DELETE) {
          //if(imageCount!=topId){
            bank.remove(topId);
            bank.put(++imageCount, currentItem);
            invalidate();
          //}
        }
        if (deleteId > 0 && currentStatus == STATUS_DELETE) {// 删除选定贴图
          if (null != stickerDelateLister) {
            if (mHashMap.containsKey(bank.get(deleteId).getHashCode())) {
              mHashMap.remove(bank.get(deleteId).getHashCode());
            }
            stickerDelateLister.OnStickerDelate(bank.get(deleteId).getHashCode());
          }
          bank.remove(deleteId);
          currentStatus = STATUS_IDLE;// 返回空闲状态

          invalidate();
        }// end if

        break;
      case MotionEvent.ACTION_MOVE:
        ret = true;
        if (currentStatus == STATUS_MOVE) {// 移动贴图
          float dx = x - oldx;
          float dy = y - oldy;
          if (currentItem != null) {
            currentItem.updatePos(dx, dy);
            invalidate();
          }// end if
          oldx = x;
          oldy = y;
        } else if (currentStatus == STATUS_ROTATE) {// 旋转 缩放图片操作
          // System.out.println("旋转");
          float dx = x - oldx;
          float dy = y - oldy;
          if (currentItem != null) {
            currentItem.updateRotateAndScale(oldx, oldy, dx, dy);// 旋转
            invalidate();
          }// end if
          oldx = x;
          oldy = y;
        }
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        ret = false;

        if (currentItem != null && currentItem instanceof StickerText) {
          if (currentStatus == STATUS_MOVE) {
            if (Math.abs(x - startx) * Math.abs(y - starty) < 5) {

              clickCount++;
              if (clickCount >= 2) {
                stickerClickLister.OnStickerTextClick((StickerText) currentItem);
              } else {
                Runnable r = new Runnable() {
                  @Override
                  public void run() {
                    if (clickCount == 1) {
                      clickCount = 0;
                    }
                  }
                };
                handler.postDelayed(r, 250);
              }
            } else {
              stickerClickLister.OnHiddenStickerTextPanel();
            }
          } else {
            stickerClickLister.OnHiddenStickerTextPanel();
          }
        }

        currentStatus = STATUS_IDLE;
        break;
    }// end switch
    return ret;
  }

  public LinkedHashMap<Integer, StickerItem> getBank() {
    return bank;
  }

  public void dragClear() {
    if (bank.size() > 0) {
      for (Integer id : bank.keySet()) {
        StickerItem item = bank.get(id);
        if (item.isDrawHelpTool = true) {
          item.isDrawHelpTool = false;
        }
      }// end for each
      invalidate();
    }
  }

  public void deleteSticker(int hashCode) {
    int deleteId = -1;
    for (Integer id : bank.keySet()) {
      StickerItem item = bank.get(id);
      if (item.getHashCode() == hashCode) {
        deleteId = id;
      }
    }
    if (deleteId != -1) {
      if (mHashMap.containsKey(hashCode)) {
        mHashMap.remove(hashCode);
      }
      bank.remove(deleteId);
      invalidate();
    }
  }

  public void deleteStickerFromPanl(int hashCode) {
    if (stickerDelateLister != null) {
      stickerDelateLister.OnStickerDelate(hashCode);
    }
  }

  public void clear() {
    bank.clear();
    this.invalidate();
  }

  public LinkedHashMap<Integer, String> getHashMap() {
    return mHashMap;
  }

  public void setHashMap(LinkedHashMap<Integer, String> hashMap) {
    mHashMap = hashMap;
  }

  public interface OnStickerClickLister {
    //绘制面板
    void OnStickerClick();

    //绘制面板
    void OnStickerTextClick(StickerText stickerText);

    void OnHiddenStickerTextPanel();
  }

  public interface OnStickerDelateLister {
    //绘制面板
    void OnStickerDelate(int hashCode);
  }

  public HashSet<Integer> getAllHashCode() {

    HashSet<Integer> hashSet = new HashSet<>();
    for (Integer id : bank.keySet()) {
      StickerItem item = bank.get(id);
      hashSet.add(item.getHashCode());
    }

    return hashSet;
  }
}// end class
