package mffs.field.thread

import resonant.api.mffs.fortron.IServerThread

/**
 * An abstract class for MFFS field calculation threads.
 * @author Calclavia
 */
abstract class AbstractFieldCalculationThread(callBack : () => Unit) extends Thread with IServerThread
{


}
