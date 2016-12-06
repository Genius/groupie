package com.genius.groupie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SectionTest {

    @Mock GroupAdapter groupAdapter;
    final int footerSize = 5;
    Group footer = new DummyGroup() {
        @Override public int getItemCount() {
            return footerSize;
        }
    };

    final int headerSize = 2;
    Group header = new DummyGroup() {
        @Override public int getItemCount() {
            return headerSize;
        }
    };

    final int placeholderSize = 3;
    Group placeholder = new DummyGroup() {
        @Override public int getItemCount() {
            return placeholderSize;
        }
    };

    Group emptyGroup = new DummyGroup() {
        @Override
        public int getItemCount() {
            return 0;
        }
    };

    @Test public void settingFooterNotifiesFooterAdded() {
        Section section = new Section();
        section.setHeader(header);
        section.add(new DummyItem());
        section.setGroupDataObserver(groupAdapter);
        section.setFooter(footer);

        verify(groupAdapter).onItemRangeInserted(section, headerSize + 1, footerSize);
    }

    @Test public void removingFooterNotifiesPreviousFooterRemoved() {
        Section section = new Section();
        section.setHeader(header);
        section.add(new DummyItem());
        section.setFooter(footer);
        section.setGroupDataObserver(groupAdapter);
        section.removeFooter();

        verify(groupAdapter).onItemRangeRemoved(section, headerSize + 1, footerSize);
    }

    @Test(expected=NullPointerException.class)
    public void settingNullFooterThrowsNullPointerException(){
        Section section = new Section();
        section.setFooter(null);
    }

    @Test public void footerCountIs0WhenThereIsNoFooter() {
        Section section = new Section();
        section.removeFooter();

        assertEquals(0, section.getItemCount());
    }

    @Test public void footerCountIsSizeOfFooter() {
        Section section = new Section();

        section.setFooter(footer);
        assertEquals(footerSize, section.getItemCount());
    }

    @Test public void settingHeaderNotifiesHeaderAdded() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setHeader(header);

        verify(groupAdapter).onItemRangeInserted(section, 0, headerSize);
    }

    @Test public void removingHeaderNotifiesPreviousHeaderRemoved() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setHeader(header);
        section.removeHeader();

        verify(groupAdapter).onItemRangeRemoved(section, 0, headerSize);
    }

    @Test(expected=NullPointerException.class)
    public void settingNullHeaderThrowsNullPointerException(){
        Section section = new Section();
        section.setFooter(null);
    }

    @Test public void headerCountIs0WhenThereIsNoHeader() {
        Section section = new Section();
        section.removeHeader();

        assertEquals(0, section.getItemCount());
    }

    @Test public void headerCountIsSizeOfHeader() {
        Section section = new Section();

        section.setHeader(header);
        assertEquals(headerSize, section.getItemCount());
    }

    @Test public void getGroup() {
        Section section = new Section();
        Item item = new DummyItem();
        section.add(item);
        assertEquals(0, section.getPosition(item));
    }

    @Test public void getPositionReturnsNegativeIfItemNotPresent() {
        Section section = new Section();
        Item item = new DummyItem();
        assertEquals(-1, section.getPosition(item));
    }

    @Test public void constructorSetsListenerOnChildren() {
        List<Group> children = new ArrayList<>();
        Item item = Mockito.mock(Item.class);
        children.add(item);
        Section section = new Section(null, children);

        verify(item).setGroupDataObserver(section);
    }

    @Test
    public void setPlaceholderOnEmptySectionAddsPlaceholder() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setGroupDataObserver(groupAdapter);
        section.setPlaceholder(placeholder);

        verify(groupAdapter).onItemRangeInserted(section, headerSize, placeholderSize);
    }

    @Test
    public void getGroupReturnsPlaceholder() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setPlaceholder(placeholder);

        assertEquals(placeholder, section.getGroup(1));
    }

    @Test
    public void setPlaceholderOnNonEmptySectionDoesNotAddPlaceholder() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.add(new DummyItem());
        section.setGroupDataObserver(groupAdapter);
        section.setPlaceholder(placeholder);

        verify(groupAdapter, never()).onItemRangeInserted(any(Section.class), anyInt(), anyInt());
    }

    @Test
    public void placeholderIsIncludedInItemCountIfBodyIsEmpty() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setPlaceholder(placeholder);

        assertEquals(headerSize + placeholderSize + footerSize, section.getItemCount());
    }

    @Test
    public void placeholderIsNotIncludedInItemCountIfBodyHasContent() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setPlaceholder(placeholder);
        section.add(new DummyItem());

        assertEquals(headerSize + footerSize + 1, section.getItemCount());
    }

    @Test
    public void addEmptyBodyContentDoesNotRemovePlaceholder() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setPlaceholder(placeholder);
        section.add(emptyGroup);

        verify(groupAdapter, never()).onItemRangeRemoved(any(Section.class), anyInt(), anyInt());
    }

    @Test
    public void addBodyContentRemovesPlaceholder() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setPlaceholder(placeholder);
        section.add(new DummyItem());

        verify(groupAdapter).onItemRangeRemoved(section, 0, placeholderSize);
    }

    @Test
    public void removeAllBodyContentAddsPlaceholder() {
        Section section = new Section();
        section.setPlaceholder(placeholder);
        Item item = new DummyItem();
        section.add(item);
        section.setGroupDataObserver(groupAdapter);
        section.remove(item);

        verify(groupAdapter).onItemRangeInserted(section, 0, placeholderSize);
    }

    @Test
    public void removeAllBodyContentByModifyingAChildGroupAddsPlaceholder() {
        Section section = new Section();
        section.setPlaceholder(placeholder);
        Section childGroup = new Section();
        Item childItem = new DummyItem();
        childGroup.add(childItem);
        section.add(childGroup);
        section.setGroupDataObserver(groupAdapter);
        childGroup.remove(childItem);

        verify(groupAdapter).onItemRangeInserted(section, 0, placeholderSize);
    }

    @Test
    public void removePlaceholderNotifies() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setPlaceholder(placeholder);
        section.setGroupDataObserver(groupAdapter);
        section.removePlaceholder();

        verify(groupAdapter).onItemRangeRemoved(section, headerSize, placeholderSize);
    }

    @Test
    public void setHideWhenEmptyRemovesAnExistingPlaceholder() {
        Section section = new Section();
        section.setPlaceholder(placeholder);
        section.setGroupDataObserver(groupAdapter);
        section.setHideWhenEmpty(true);

        verify(groupAdapter).onItemRangeRemoved(section, 0, placeholderSize);
    }

    @Test
    public void setHeaderAddsHeader() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setHeader(header);

        verify(groupAdapter).onItemRangeInserted(section, 0, headerSize);
    }

    @Test
    public void removeHeaderRemovesHeader() {
        Section section = new Section();
        section.setHeader(header);
        section.setGroupDataObserver(groupAdapter);
        section.removeHeader();

        verify(groupAdapter).onItemRangeRemoved(section, 0, headerSize);
    }

    @Test
    public void setFooterAddsFooter() {
        Section section = new Section();
        section.setGroupDataObserver(groupAdapter);
        section.setFooter(footer);

        verify(groupAdapter).onItemRangeInserted(section, 0, footerSize);
    }

    @Test
    public void removeFooterRemovesFooter() {
        Section section = new Section();
        section.setFooter(footer);
        section.setGroupDataObserver(groupAdapter);
        section.removeFooter();

        verify(groupAdapter).onItemRangeRemoved(section, 0, footerSize);
    }

    @Test
    public void setHideWhenEmptyRemovesExistingHeaderAndFooter() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setGroupDataObserver(groupAdapter);
        section.setHideWhenEmpty(true);

        verify(groupAdapter).onItemRangeRemoved(section, 0, headerSize + footerSize);
    }

    @Test
    public void setHideWhenEmptyRemovesExistingHeaderFooterAndPlaceholder() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setPlaceholder(placeholder);
        section.setGroupDataObserver(groupAdapter);
        section.setHideWhenEmpty(true);

        verify(groupAdapter).onItemRangeRemoved(section, 0, headerSize + footerSize + placeholderSize);
    }

    @Test
    public void setHideWhenEmptyFalseAddsExistingHeaderAndFooter() {
        Section section = new Section();
        section.setHeader(header);
        section.setFooter(footer);
        section.setHideWhenEmpty(true);
        section.setGroupDataObserver(groupAdapter);
        section.setHideWhenEmpty(false);

        verify(groupAdapter).onItemRangeInserted(section, 0, headerSize);
        verify(groupAdapter).onItemRangeInserted(section, headerSize, footerSize);
    }

    @Test
    public void itemCountIsZeroWhenSetHideWhenEmptyTrue() {
        Section section = new Section();
        section.setHeader(header);
        section.setPlaceholder(placeholder);
        section.setFooter(footer);
        section.setHideWhenEmpty(true);

        assertEquals(0, section.getItemCount());
    }

    @Test
    public void groupCountIsHeaderFooterAndChildrenWhenNonEmpty() {
        Section section = new Section();
        section.setHeader(header);
        section.setPlaceholder(placeholder);
        section.setFooter(footer);
        section.add(new DummyItem());
        section.add(new DummyItem());

        assertEquals(4, section.getGroupCount());
    }

    @Test
    public void groupCountIsHeaderFooterAndPlaceholderWhenEmpty() {
        Section section = new Section();
        section.setHeader(header);
        section.setPlaceholder(placeholder);
        section.setFooter(footer);

        assertEquals(3, section.getGroupCount());
    }

    @Test
    public void groupCountIsZeroWhenEmptyAndSetHideWhenEmpty() {
        Section section = new Section();
        section.setHeader(header);
        section.setPlaceholder(placeholder);
        section.setFooter(footer);
        section.setHideWhenEmpty(true);

        assertEquals(0, section.getGroupCount());
    }

    @Test
    public void whenSectionIsEmptyAndSetHideWhenEmptyGetGroupReturnsNull() {
        Section section = new Section();
        section.setHeader(header);
        section.setPlaceholder(placeholder);
        section.setFooter(footer);
        section.setHideWhenEmpty(true);

        assertNull(section.getGroup(0));
    }
}
