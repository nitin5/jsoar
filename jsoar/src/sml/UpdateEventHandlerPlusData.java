/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.35
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package sml;

public class UpdateEventHandlerPlusData extends EventHandlerPlusData {
    Kernel.UpdateEventInterface m_Handler;
    
    /*
  private long swigCPtr;

  protected UpdateEventHandlerPlusData(long cPtr, boolean cMemoryOwn) {
    super(smlJNI.SWIGUpdateEventHandlerPlusDataUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(UpdateEventHandlerPlusData obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }
*/
  public synchronized void delete() {
      /*
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      smlJNI.delete_UpdateEventHandlerPlusData(swigCPtr);
    }
    swigCPtr = 0;
    */
    super.delete();
  }

  public void setM_Handler(SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void value) {
    //smlJNI.UpdateEventHandlerPlusData_m_Handler_set(swigCPtr, this, SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void.getCPtr(value));
    throw new UnsupportedOperationException();
  }

  public SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void getM_Handler() {
//    long cPtr = smlJNI.UpdateEventHandlerPlusData_m_Handler_get(swigCPtr, this);
//    return (cPtr == 0) ? null : new SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void(cPtr, false);
    throw new UnsupportedOperationException();
  }

  public UpdateEventHandlerPlusData() {
    //this(smlJNI.new_UpdateEventHandlerPlusData__SWIG_0(), true);
  }

  public UpdateEventHandlerPlusData(int eventID, SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void handler, SWIGTYPE_p_void userData, int callbackID) {
    //this(smlJNI.new_UpdateEventHandlerPlusData__SWIG_1(eventID, SWIGTYPE_p_f_enum_sml__smlUpdateEventId_p_void_p_sml__Kernel_enum_sml__smlRunFlags__void.getCPtr(handler), SWIGTYPE_p_void.getCPtr(userData), callbackID), true);
  }

}
