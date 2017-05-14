/*
 * The MIT License
 *
 * Copyright 2014 Florian Poulin - https://github.com/fpoulin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package la.alsocan.symbiot.api.to.bindings;

import la.alsocan.jsonshapeshifter.bindings.Binding;
import la.alsocan.jsonshapeshifter.bindings.BooleanNodeBinding;
import la.alsocan.symbiot.core.streams.Stream;

/**
 * @author Florian Poulin - https://github.com/fpoulin
 */
public class BooleanNodeBindingTo extends AbstractNodeBindingTo {

	public static final String TYPE = "booleanNode";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public Binding<Boolean> build(Stream s){
		return new BooleanNodeBinding(s.getT().getSource().at(sourceNode));
	}
}
