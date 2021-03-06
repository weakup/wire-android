/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.common.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import com.waz.api.AccentColor
import com.waz.model.EphemeralDuration
import com.waz.utils.events.Signal
import com.waz.zclient.paintcode.{EphemeralIcon, HourGlassIcon}
import com.waz.zclient.ui.text.TypefaceTextView
import com.waz.zclient.utils.ContextUtils.{getColor, getDimenPx}
import com.waz.zclient.utils.RichView
import com.waz.zclient.{R, ViewHelper}

import scala.concurrent.duration.FiniteDuration

class EphemeralCursorButton(context: Context, attrs: AttributeSet, defStyleAttr: Int) extends TypefaceTextView(context, attrs, defStyleAttr) with ViewHelper {
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  val accentColor = inject[Signal[AccentColor]]
  val ephemeralExpiration = Signal[Option[FiniteDuration]](None)

  val lengthAndUnit = ephemeralExpiration.map(_.map(EphemeralDuration(_)))
  val (len, unit) =
    (lengthAndUnit.map(_.map(_._1)), lengthAndUnit.map(_.map(_._2)))

  val display = len.map(_.map(_.toString).getOrElse(""))

  val color =
    ephemeralExpiration.flatMap {
      case Some(_) => accentColor.map(_.getColor)
      case _ => Signal.const(getColor(R.color.text__primary_dark))
    }

  val iconSize = ephemeralExpiration.map {
    case Some(_) => R.dimen.wire__padding__24
    case _       => R.dimen.wire__padding__16
  }.map(getDimenPx)

  //For QA testing
  val contentDescription = lengthAndUnit.map {
    case Some((l, unit)) => s"$l$unit"
    case None => "off"
  }

  val drawable: Signal[Drawable] =
    for {
      color <- color
      unit  <- unit
    } yield {
      unit match {
        case Some(u) => EphemeralIcon(color, u)
        case _       => HourGlassIcon(color)
      }
    }

  override def onFinishInflate(): Unit = {
    super.onFinishInflate()

    setGravity(Gravity.CENTER)

    display.onUi(setText)

    drawable.onUi(setBackgroundDrawable)
    contentDescription.onUi(setContentDescription)

    color.onUi(setTextColor)
    iconSize.onUi { size =>
      this.setWidthAndHeight(Some(size), Some(size))
    }
  }
}
