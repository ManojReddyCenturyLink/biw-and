package com.centurylink.biwf.screens.changeappointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.databinding.AppointmentSlotsItemsBinding
import com.centurylink.biwf.R

/**
 * Appointment slots adapter
 *
 * @property slotList - The list of slots for appointment
 * @property slotSelectListener - This will listen to Selected slots
 * @constructor Create empty Appointment slots adapter
 */
class AppointmentSlotsAdapter(
    var slotList: List<String>,
    private val slotSelectListener: SlotClickListener
) : RecyclerView.Adapter<AppointmentSlotsAdapter.SlotsViewHolder>() {

    var lastSelectedPosition = -1
    var isError: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotsViewHolder {
        return SlotsViewHolder(AppointmentSlotsItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: SlotsViewHolder, position: Int) {
        val slotContent: String = slotList[position]
        holder.bind(slotContent, position)
    }

    override fun getItemCount(): Int = slotList.size

    /**
     * Slots view holder
     *
     * @property binding - This binds appointment slots items
     * @constructor Create empty Slots view holder
     */
    inner class SlotsViewHolder(private var binding: AppointmentSlotsItemsBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind - This handles binding logic for provided slots information
         *
         * @param slotInfo - slot information
         * @param position - The position to check last selected radio button
         */
        fun bind(slotInfo: String, position: Int) {
            if(isError) {
                binding.appointmentSelectRadioBtn.setBackgroundResource(R.drawable.ic_radio_error)
            } else {
                binding.appointmentSelectRadioBtn.setBackgroundResource(R.drawable.radiobutton_selector)
            }
            binding.appointmentTime.text = slotInfo
            binding.appointmentSelectRadioBtn.setOnClickListener {
                lastSelectedPosition = adapterPosition
                notifyDataSetChanged()
                slotSelectListener.onSlotSelected(slotList[lastSelectedPosition])
            }
            binding.appointmentSelectRadioBtn.isChecked = position == lastSelectedPosition
        }
    }

    /**
     * Slot click listener - Interface to handle slot click listeners
     *
     * @constructor Create empty Slot click listener
     */
    interface SlotClickListener {

        /**
         * On slot selected - Abstract method to implemented
         *
         * @param slotInfo - Slot information
         */
        fun onSlotSelected(slotInfo: String)

    }
}