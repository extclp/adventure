/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.examination.ExaminableProperty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

final class GlobalTranslationSourceImpl implements GlobalTranslationSource {
  static final GlobalTranslationSourceImpl INSTANCE = new GlobalTranslationSourceImpl();
  final TranslatableComponentRenderer<Locale> renderer = TranslatableComponentRenderer.usingTranslationSource(this);
  private final Set<Key> keys = Collections.synchronizedSet(new HashSet<>());
  private final List<Map.Entry<Key, TranslationSource>> sources = new CopyOnWriteArrayList<>();

  private GlobalTranslationSourceImpl() {
  }

  @Override
  public void register(final @NonNull Key key, final @NonNull TranslationSource source) {
    requireNonNull(key, "key");
    requireNonNull(source, "source");
    if(source == this) {
      throw new IllegalArgumentException("cannot register GlobalTranslationSource to itself");
    }
    if(!this.keys.add(key)) {
      throw new IllegalStateException("translation source already registered for " + key);
    }
    this.sources.add(new SimpleImmutableEntry<>(key, source));
  }

  @Override
  public void unregister(final @NonNull Key key) {
    requireNonNull(key, "key");
    if(this.keys.remove(key)) {
      this.sources.removeIf(e -> e.getKey().equals(key));
    }
  }

  @Override
  public @Nullable MessageFormat translate(final @NonNull String key, final @NonNull Locale locale) {
    requireNonNull(key, "key");
    requireNonNull(locale, "locale");
    for(final Map.Entry<Key, TranslationSource> translation : this.sources) {
      final MessageFormat result = translation.getValue().translate(key, locale);
      if(result != null) {
        return result;
      }
    }
    return null;
  }

  @Override
  public @NonNull Stream<? extends ExaminableProperty> examinableProperties() {
    return Stream.of(ExaminableProperty.of("sources", this.sources));
  }
}
